import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Zdzislaw on 17.04.2017.
 */

interface Rysowanie extends Remote
{
    public byte[] setrmi() throws RemoteException;
    public byte[] rysujrmi(Point p, Color c, Object rozmiarZSpinera, int capRound, int joinRound, float miterLimit) throws RemoteException;
    public byte[] piszrmi(String s, Point p, Color c, int capRound, int joinRound, float miterLimit) throws RemoteException;
    public byte[] wyczyscrmi() throws RemoteException;
    public byte[] odczytajrmi(byte[] input) throws RemoteException;
}
class RysowanieI extends UnicastRemoteObject implements Rysowanie
{
    private static BufferedImage common;
    private Map<RenderingHints.Key, Object> hm;
    private RenderingHints rh;

    private JPanel obszarJP;

    public RysowanieI() throws RemoteException
    {
        super();
        hm = new HashMap<RenderingHints.Key,Object>();
        hm.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hm.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        hm.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        rh = new RenderingHints(hm);
        common = new BufferedImage(1300,650,BufferedImage.TYPE_INT_ARGB);

        obszarJP = new JPanel();

        Graphics2D constr = common.createGraphics();
        constr.setColor(Color.WHITE);
        constr.fillRect(0,0,common.getWidth(),common.getHeight());
        constr.setRenderingHints(rh);


    }
    public byte[] setrmi()
    {
        ByteArrayOutputStream bit = new ByteArrayOutputStream();
        try {
            ImageIO.write(common,"jpg",bit);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("SETTED");
        return bit.toByteArray();
    }
    public byte[] rysujrmi(Point p, Color c, Object rozmiarZSpinera, int capRound, int joinRound, float miterLimit)
    {
        String rZS= rozmiarZSpinera.toString();
        rZS = rZS.replaceAll("[^\\d.]", "");
        int rozmiarSpinner = Integer.parseInt(rZS);

        int i=0;
        Graphics2D g = common.createGraphics();
        g.setRenderingHints(rh);
        g.setColor(c);
        g.setStroke(new BasicStroke(rozmiarSpinner, capRound, joinRound, miterLimit));
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

    public byte[] piszrmi(String s, Point p, Color c, int capRound, int joinRound, float miterLimit) throws RemoteException {


        Graphics2D g = common.createGraphics();
        g.setRenderingHints(rh);
        g.setColor(c);
        g.setStroke(new BasicStroke(3, capRound, joinRound, miterLimit));
        g.drawString(s, p.x ,p.y);
        g.dispose();

        ByteArrayOutputStream bit = new ByteArrayOutputStream();
        try {
            ImageIO.write(common,"jpg",bit);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bit.toByteArray();
    }

    public byte[] wyczyscrmi()
    {
        Graphics2D g = common.createGraphics();
        g.setRenderingHints(rh);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, common.getWidth(), common.getHeight());

        g.dispose();
        //obszarLabel.repaint();

        ByteArrayOutputStream bit = new ByteArrayOutputStream();
        try {
            ImageIO.write(common,"jpg",bit);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bit.toByteArray();
    }



    public byte[] odczytajrmi(byte[] input) throws RemoteException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(input);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            BufferedImage tmp = ImageIO.read(bais);
            common.createGraphics().drawImage(tmp,0,0,Color.WHITE,null);
            ImageIO.write(common,"jpg",baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

}

public class RysowanieServer extends JFrame
{
    private JPanel kontener;
    private JTextField portPole;
    private JLabel portNapis;
    private int nrPortu = 1099;
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
                nrPortu = Integer.parseInt(portPole.getText());
                if(nrPortu==80 || nrPortu==81 || nrPortu==443 || nrPortu==53)
                {
                    JOptionPane.showMessageDialog(uruchom,"Zajęty numer portu");

                }
                else {
                    System.out.println(nrPortu);
                    serwer = new Server();
                    serwer.start();

                    uruchom.setEnabled(false);
                    zatrzymaj.setEnabled(true);
                    portPole.setEnabled(false);
                    repaint();
                }
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
        public Registry registry;
        public void ubijSerwer()
        {
            System.out.println("Zatrzymuje...");
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
                try
                {
                    System.out.println("Uruchamiam...");
                    registry = LocateRegistry.createRegistry(nrPortu);
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
