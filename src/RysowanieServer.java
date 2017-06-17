
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created on 17.04.2017.
 */

public class RysowanieServer extends JFrame
{
    private JPanel kontener;
    private JTextField portPole;
    private JLabel portNapis;
    private int nrPortu = 1099;
    private JButton uruchom, zatrzymaj;
    //private JButton klient;
    private BTGradient klient;
    private JTextArea komunikaty;
    RysowanieServer referencjaSerwera;

    public RysowanieServer()
    {
        referencjaSerwera = this;
        setTitle("Serwer");
        setSize(450, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout()); //wg kierunkow

        this.setIconImage(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("rysowanieServer.png")));

        kontener = new JPanel(new FlowLayout()); //w kontenerze poziomo
        portNapis = new JLabel("Port RMI: ");
        portPole = new JTextField(new Integer(nrPortu).toString(), 8);
        uruchom = new JButton("Uruchom");
        zatrzymaj = new JButton("Zatrzymaj");
        klient = new BTGradient("Klient");
        zatrzymaj.setEnabled(false);
        klient.setEnabled(false);

        komunikaty = new JTextArea();
        komunikaty.setLineWrap(true);
        komunikaty.setEditable(false);

        Zdarzenia obsluga = new Zdarzenia();
        uruchom.addActionListener(obsluga);
        zatrzymaj.addActionListener(obsluga);
        klient.addActionListener(obsluga);

        kontener.add(portNapis);
        kontener.add(portPole);
        kontener.add(uruchom);
        kontener.add(zatrzymaj);
        kontener.add(klient);

        add(kontener, BorderLayout.NORTH);
        add(new JScrollPane(komunikaty), BorderLayout.CENTER);

        setVisible(true);
    }

    private class Zdarzenia implements ActionListener
    {
        private Server serwer;

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getActionCommand().equals("Uruchom"))
            {
                nrPortu = Integer.parseInt(portPole.getText());
                if(nrPortu==80 || nrPortu==81 || nrPortu==443 || nrPortu==53)
                {
                    JOptionPane.showMessageDialog(uruchom,"Zajęty numer portu");

                }
                else {
                    //System.out.println(nrPortu);
                    serwer = new Server();
                    serwer.start();

                    uruchom.setEnabled(false);
                    zatrzymaj.setEnabled(true);
                    portPole.setEnabled(false);
                    klient.setEnabled(true);
                    repaint();
                }
            }
            if (e.getActionCommand().equals("Zatrzymaj"))
            {
                serwer.ubijSerwer();

                zatrzymaj.setEnabled(false);
                uruchom.setEnabled(true);
                portPole.setEnabled(true);
                klient.setEnabled(false);
                repaint();
            }
            if (e.getActionCommand().equals("Klient"))
            {
                //try {
                new RysowanieClient(Integer.parseInt(portPole.getText()));
                //klient.setEnabled(false);
                //Thread.sleep(2000);
                //klient.setEnabled(true);
                //} catch (InterruptedException e1) {
                //   e1.printStackTrace();
                //}
            }

        }
    }



    private class Server extends Thread
    {
        public Registry registry;
        public void ubijSerwer()
        {
            //System.out.println("Zatrzymuje...");
            wyswietlKomunikat("Zatrzymanie serwera");
            try {
                registry.unbind("RDraw");
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (NotBoundException e) {
                e.printStackTrace();
            }
            this.stop();

        }

            public void run()
            {
                try {
                    //System.out.println("Uruchamiam...");
                    registry = LocateRegistry.createRegistry(nrPortu);
                    wyswietlKomunikat("Rejestracja na porcie: " + portPole.getText());
                }catch (Exception e){
                 wyswietlKomunikat("Nie powiodło sie utworzenie rejestru na tym porcie. \n Spróbuj ponownie lub skorzystaj z innego.");
                }
                if (registry == null) {
                    try {
                        registry = LocateRegistry.getRegistry();
                    } catch (Exception e) {
                        wyswietlKomunikat("Brak uruchomionego rejestru.");
                    }
                }

                try {
                    RysowanieImpl remote = new RysowanieImpl();
                    registry.bind("RDraw",remote);
                    //System.out.println("Uruchomiony");
                    wyswietlKomunikat("Serwer zarejestrowany i uruchomiony. Możesz uruchamiać klientów.");
                }catch (Exception e){
                    wyswietlKomunikat("Nie udało się uruchomić serwera.");
                }

            }
    }

    private static final class BTGradient extends JButton{

        private BTGradient(String tytul){
            super(tytul);
            setContentAreaFilled(false);
        }

        protected void paintComponent(Graphics g){
            Graphics2D graphics2D = (Graphics2D) g.create();
            graphics2D.setPaint(new GradientPaint(
                    new Point(0, 0),
                    getBackground(),
                    new Point(0, getHeight()/3),
                    Color.WHITE));

            graphics2D.fillRect(0, 0, getWidth(), getHeight()/3);
            graphics2D.setPaint(new GradientPaint(
                    new Point(0, getHeight()/3),
                    Color.WHITE,
                    new Point(0, getHeight()),
                    Color.LIGHT_GRAY));
            graphics2D.fillRect(0, getHeight()/3, getWidth(), getHeight());
            graphics2D.dispose();

            super.paintComponent(g);
        }
    }

    public void wyswietlKomunikat(String tekst)
    {
        komunikaty.append(tekst + "\n");
        komunikaty.setCaretPosition(komunikaty.getDocument().getLength());
    }

    public static void main(String[] args) {
        new RysowanieServer();
    }
}
