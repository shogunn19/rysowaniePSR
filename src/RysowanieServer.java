import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.registry.Registry;

/**
 * Created by Zdzislaw on 17.04.2017.
 */
public class RysowanieServer extends JFrame
{
    private JPanel kontener;
    private JTextField portPole;
    private JLabel portNapis;
    private final int nrPortu = 1099;
    private JButton uruchom, zatrzymaj;
    private JTextArea komunikaty;
    RysowanieServer referencjaSerwera;

    public RysowanieServer()
    {
        referencjaSerwera = this;
        setTitle("Serwer");
        setSize(450, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout()); //wg kierunkow

        kontener = new JPanel(new FlowLayout()); //w kontenerze poziomo
        portNapis = new JLabel("Port RMI: ");
        portPole = new JTextField(new Integer(nrPortu).toString(), 8);
        uruchom = new JButton("Uruchom");
        zatrzymaj = new JButton("Zatrzymaj");
        zatrzymaj.setEnabled(false);

        komunikaty = new JTextArea();
        komunikaty.setLineWrap(true);
        komunikaty.setEditable(false);

        Zdarzenia obsluga = new Zdarzenia();
        uruchom.addActionListener(obsluga);
        zatrzymaj.addActionListener(obsluga);

        kontener.add(portNapis);
        kontener.add(portPole);
        kontener.add(uruchom);
        kontener.add(zatrzymaj);

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
                serwer = new Server();
                serwer.start();

                uruchom.setEnabled(false);
                zatrzymaj.setEnabled(true);
                portPole.setEnabled(false);
                repaint();
            }
            if (e.getActionCommand().equals("Zatrzymaj"))
            {
                serwer.ubijSerwer();

                zatrzymaj.setEnabled(false);
                uruchom.setEnabled(true);
                portPole.setEnabled(true);
                repaint();
            }

        }
    }

    private class Server extends Thread
    {
        public void ubijSerwer()
        {
            Registry rejestr;

            try
            {
                //rejestr.unbind("RysowanieRMI");
                //wyswietlKomunikat("Serwer został wyrejestrowany.");
            }
            catch (Exception e)
            {
                //wyswietlKomunikat("Nie udało się wyrejestrować serwera.");

            }
        }

            public void run()
            {

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
