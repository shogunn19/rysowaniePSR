import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Zdzislaw on 17.04.2017.
 */

interface Rysowanie extends Remote
{
    public byte[] rysujrmi(Point p) throws RemoteException;
    public byte[] setrmi() throws RemoteException;
}
class RysowanieI extends UnicastRemoteObject implements Rysowanie
{
    private static BufferedImage common;
    private Map<RenderingHints.Key, Object> hm;
    private RenderingHints rh;


    public RysowanieI() throws RemoteException
    {
        super();
        hm = new HashMap<RenderingHints.Key,Object>();
        hm.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hm.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        hm.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        rh = new RenderingHints(hm);
        common = new BufferedImage(1300,650,BufferedImage.TYPE_INT_ARGB);

    }
    public byte[] setrmi()
    {
        Graphics2D g = common.createGraphics();
        g.setRenderingHints(rh);
        g.setColor(Color.BLACK);
        g.dispose();
        ByteArrayOutputStream bit = new ByteArrayOutputStream();
        try {
            ImageIO.write(common,"jpg",bit);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("SETTED");
        return bit.toByteArray();
    }
    public byte[] rysujrmi(Point p)
    {
        int i=0;
        Graphics2D g = common.createGraphics();
        g.setRenderingHints(rh);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(3,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,1.7f));
        g.drawLine(p.x,p.y,p.x+i,p.y+i);
        //g.repaint()
        g.dispose();
        ByteArrayOutputStream bit = new ByteArrayOutputStream();
        try {
            ImageIO.write(common,"jpg",bit);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("DRAWED");
        return bit.toByteArray();
    }
}

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
                try
                {
                    System.out.println("Uruchamiam...");
                    Registry registry = LocateRegistry.createRegistry(1099);
                    RysowanieI remote = new RysowanieI();
                    registry.bind("RDraw",remote);
                    System.out.println("Uruchomiony");
                }catch (Exception e){e.printStackTrace();}
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
