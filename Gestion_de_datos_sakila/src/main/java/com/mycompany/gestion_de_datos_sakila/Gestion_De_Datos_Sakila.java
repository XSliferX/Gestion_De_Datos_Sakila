 package com.mycompany.gestion_de_datos_sakila;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.sound.sampled.LineEvent.Type;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class Gestion_De_Datos_Sakila {

    private Connection connection;
    private JFrame frame;
    private Clip[] audioClips;
    private int currentClipIndex = 0;

    public Gestion_De_Datos_Sakila() {
        try {
            String dbURL = "jdbc:mysql://localhost:3306/sakila";
            String username = "root";
            String password = "";
            connection = DriverManager.getConnection(dbURL, username, password);
        } catch (SQLException e) {
            handleError("Error al conectar a la base de datos.", e);
        }

        setLookAndFeel();

        initializeFrame();
        createUI();
        initializeAudioClips();
        playCurrentAudioClip();

        // Configurar un escuchador para el final del audio actual
        audioClips[currentClipIndex].addLineListener(event -> {
            if (event.getType() == Type.STOP) {
                playNextAudioClip();
            }
        });
    }

    private void handleError(String message, Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
            handleError("Error al configurar el aspecto del sistema.", e);
        }
    }

    private void initializeFrame() {
        frame = new JFrame("Base de Datos Sakila");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Configuración del fondo de la ventana
        JLabel background = new JLabel(new ImageIcon("lago.jpg"));
        background.setLayout(new BorderLayout());
        frame.setContentPane(background);

        frame.setSize(900, 700);
        frame.setResizable(false); // Evitar que la ventana sea maximizable
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void createUI() {
        JMenuBar menuBar = new JMenuBar();

        // Menú SQL3Shakila4
        JMenu menu1 = new JMenu("Menú-1");

        String[] tableNames1 = {"payment", "rental", "store", "payment-customer", "customer", "staff"};

        for (String tableName : tableNames1) {
            JMenuItem menuItem = new JMenuItem("Info de la tabla " + tableName);
            menuItem.addActionListener(e -> displayTableInfo(tableName));
            menu1.add(menuItem);
        }
        JMenuItem paymentSubqueryMenuItem = new JMenuItem("Consulta de pagos con subconsulta");
        paymentSubqueryMenuItem.addActionListener(e -> displayPaymentSubquery());
        menu1.add(paymentSubqueryMenuItem);
        // Menú SQL3Shakila5
        JMenu menu2 = new JMenu("Menú-2");

        JMenuItem infoCiudadesMenuItem5 = new JMenuItem("<html><b>Info de ciudades – país</b></html>");
        infoCiudadesMenuItem5.addActionListener(e -> displayCiudadesPorPais());
        menu2.add(infoCiudadesMenuItem5);

        JMenuItem infoPelisPorDuracionMenuItem5 = new JMenuItem("<html><b>Info de películas por duración</b></html>");
        infoPelisPorDuracionMenuItem5.addActionListener(e -> displayPelisPorDuracion());
        menu2.add(infoPelisPorDuracionMenuItem5);

        JMenuItem infoEmpleadosMenuItem5 = new JMenuItem("<html><b>Info de empleados</b></html>");
        infoEmpleadosMenuItem5.addActionListener(e -> displayInfoEmpleados());
        menu2.add(infoEmpleadosMenuItem5);

        menuBar.add(menu1);
        menuBar.add(menu2);

        // Menú de canciones
        JMenu songsMenu = new JMenu("Canciones");
        menuBar.add(songsMenu);

        JMenuItem playSongsMenuItem = new JMenuItem("Elegir canción");
        playSongsMenuItem.addActionListener(e -> playSongs());
        songsMenu.add(playSongsMenuItem);

        frame.setJMenuBar(menuBar);
    }

    private void displayTableInfo(String tableName) {
        String query = getQueryForTable(tableName);
        DefaultTableModel tableModel = new DefaultTableModel();

        try (PreparedStatement statement = connection.prepareStatement(query); ResultSet resultSet = statement.executeQuery()) {

            int columnCount = resultSet.getMetaData().getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                tableModel.addColumn(resultSet.getMetaData().getColumnName(i));
            }

            while (resultSet.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = resultSet.getObject(i);
                }
                tableModel.addRow(row);
            }

            JTable table = new JTable(tableModel);
            formatTable(table);
            JOptionPane.showMessageDialog(frame, new JScrollPane(table), "Resultados de la consulta", JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException e) {
            handleError("Error al ejecutar la consulta.", e);
        }
    }

    private String getQueryForTable(String tableName) {
        // La lógica para obtener la consulta SQL de cada tabla puede ser compleja
        // Se podría implementar aquí o incluso en métodos separados para mayor claridad
        // Por ahora, se deja como un simple placeholder.
        return "SELECT * FROM " + tableName;
    }

    private void formatTable(JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 70;
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                Component c = table.prepareRenderer(cellRenderer, row, column);
                width = Math.max(c.getPreferredSize().width + 1, width);
            }
            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }

/**
 * Muestra los resultados de una subconsulta compleja relacionada con pagos en la base de datos Sakila.
 * La subconsulta selecciona información detallada sobre pagos, clientes, alquileres, inventario, películas y tiendas asociadas.
 * Solo se incluyen pagos con un monto mayor a 3.99, y los resultados se ordenan por el primer nombre del cliente.
 * Esto lo hago porque quería probar hacer algo con una subconsulta, no es de lo que pediste.
 */
private void displayPaymentSubquery() {
    // Consulta SQL que realiza la subconsulta
    String query = "SELECT p.payment_id, p.customer_id, p.amount, p.payment_date, c.first_name AS customer_name, "
            + "r.inventory_id, f.title AS film_title, s.store_id, s.manager_staff_id, "
            + "py.rental_id, py.return_date "
            + "FROM payment p "
            + "JOIN customer c ON c.customer_id = p.customer_id "
            + "JOIN rental py ON py.rental_id = p.rental_id "
            + "JOIN inventory r ON r.inventory_id = py.inventory_id "
            + "JOIN film f ON f.film_id = r.film_id "
            + "JOIN store s ON s.store_id = r.store_id "
            + "WHERE p.amount > 3.99 "
            + "ORDER BY c.first_name";

    // Ejecuta la consulta y muestra los resultados en una ventana de diálogo
    executeAndDisplayQuery(query);
}


    private void displayCiudadesPorPais() {
        String pais = JOptionPane.showInputDialog(frame, "Ingrese el país en inglés (por ejemplo, Spain):");
        if (pais != null) {
            String query = "SELECT city FROM city WHERE country_id IN (SELECT country_id FROM country WHERE country = ?)";
            executeAndDisplayQuery(query, pais);
        }
    }

    private void displayPelisPorDuracion() {
        try {
            int duracionMinima = Integer.parseInt(JOptionPane.showInputDialog(frame, "Duración mínima en minutos:"));
            int duracionMaxima = Integer.parseInt(JOptionPane.showInputDialog(frame, "Duración máxima en minutos:"));

            String query = "SELECT title, length FROM film WHERE length BETWEEN ? AND ?";
            executeAndDisplayQuery(query, duracionMinima, duracionMaxima);
        } catch (NumberFormatException e) {
            handleError("Ingrese valores numéricos válidos para la duración.", e);
        }
    }

private void displayInfoEmpleados() {
    String pais = JOptionPane.showInputDialog(frame, "Ingrese el país:");
    String ciudad = JOptionPane.showInputDialog(frame, "Ingrese la ciudad:");

    if (pais != null && ciudad != null) {
        String query = "SELECT * FROM staff "
                + "WHERE address_id = ("
                + "SELECT address.address_id FROM address "
                + "INNER JOIN staff ON address.address_id = staff.address_id "
                + "INNER JOIN city ON address.city_id = city.city_id "
                + "INNER JOIN country ON country.country_id = city.country_id "
                + "WHERE city.city = ? AND country.country = ?)";

        executeAndDisplayQuery(query, ciudad, pais);
    }
}



    private void executeAndDisplayQuery(String query, Object... parameters) {
        DefaultTableModel tableModel = new DefaultTableModel();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }

            ResultSet resultSet = statement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                tableModel.addColumn(metaData.getColumnName(i));
            }

            while (resultSet.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = resultSet.getObject(i);
                }
                tableModel.addRow(row);
            }

            JTable table = new JTable(tableModel);
            formatTable(table);
            JOptionPane.showMessageDialog(frame, new JScrollPane(table), "Resultados de la consulta", JOptionPane.PLAIN_MESSAGE);

        } catch (SQLException e) {
            handleError("Error al ejecutar la consulta.", e);
        }
    }

    private void playSongs() {
        String[] songNames = {"Hoy.wav", "IDWMAT.wav", "Iris.wav", "MJTCU.wav", "EBYT.wav", "DJGFU.wav",
            "Flaca.wav", "ElMundoTrasElCristal.wav", "TheReason.wav", "ChasingCars.wav"};

        String songChoice = (String) JOptionPane.showInputDialog(frame, "Elige una canción:", "Canciones", JOptionPane.QUESTION_MESSAGE, null, songNames, songNames[0]);

        if (songChoice != null) {
            // Detener la reproducción actual antes de cambiar de canción
            stopCurrentAudioClip();

            for (int i = 0; i < audioClips.length; i++) {
                if (songChoice.equals(songNames[i])) {
                    currentClipIndex = i;
                    playCurrentAudioClip();
                    break;
                }
            }
        }
    }

    private void stopCurrentAudioClip() {
        audioClips[currentClipIndex].stop();
    }

    private void initializeAudioClips() {
        audioClips = new Clip[10];

        try {
            String[] songNames = {"Hoy.wav", "IDWMAT.wav", "Iris.wav", "MJTCU.wav", "EBYT.wav", "DJGFU.wav",
                "Flaca.wav", "ElMundoTrasElCristal.wav", "TheReason.wav", "ChasingCars.wav"};

            for (int i = 0; i < audioClips.length; i++) {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(songNames[i]));
                audioClips[i] = AudioSystem.getClip();
                audioClips[i].open(audioInputStream);
            }

        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            handleError("Error al inicializar los clips de audio.", e);
        }
    }

    private void playNextAudioClip() {
        currentClipIndex = (currentClipIndex + 1) % audioClips.length;
        playCurrentAudioClip();
    }

    private void playCurrentAudioClip() {
        audioClips[currentClipIndex].setFramePosition(0);
        audioClips[currentClipIndex].start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Gestion_De_Datos_Sakila::new);
    }
}
