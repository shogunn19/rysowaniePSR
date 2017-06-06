import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.awt.*;

/**
 * Created on 17.04.2017.
 */


public class RysowanieClient extends JFrame
{
    private JPanel obszar;
    private JPanel kontenerPrzyciskGora, kontenerPrzyciskiDol ;
    private JToolBar kontenerPrzybornik;
    private JButton zapis, wczytywanie, wyczysc, kolorBT;
    private JLabel przybornikLabel, obszarLabel, rozmiarLabel;
    private JRadioButton rysuj, pisz;
    private JSpinner rozmiarRysowania;
    private SpinnerNumberModel rozmiarRysowaniaTryb;
    //private JScrollPane obszarScroll;


    public static final int RYSOWANIE_NARZEDZIE = 0;
    public static final int TEXT_NARZEDZIE = 1;
    private int aktywneNarzedzie;

    private BufferedImage BIwyjsciowyObszarRob;
    private BufferedImage BIzmienianyObszarRob;
    private BufferedImage probkaKoloru = new BufferedImage(16,16,BufferedImage.TYPE_INT_RGB);
    private RenderingHints rh;
    private Color kolor;
    private int nrPortu;

    private Point zaznaczeniePunktPoczatkowy;
    private Rectangle zaznaczenie;
    public Stroke obrys = new BasicStroke(7,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,1.7f);
    //private Obryss obrys = new Obryss(3,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,1.7f);

    /* zmienne do konstruktora obrysu*/
    final int cap_round = 1, join_round = 1;
    final float miterlimit = 1.7f;

    private Registry registry;
    private Rysowanie rys;


    public RysowanieClient(int port)
    {
        setTitle("Klient");
        //setSize(Toolkit.getDefaultToolkit().getScreenSize());
        //this.setState(JFrame.MAXIMIZED_BOTH);
        //setSize(600,600);
        nrPortu = port;
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        this.setIconImage(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("rysowanieRMIico.png")));
        Map<RenderingHints.Key, Object> hm = new HashMap<RenderingHints.Key,Object>();
        hm.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hm.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        hm.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        rh = new RenderingHints(hm);
        ustawObszar(new BufferedImage(1300, 650, BufferedImage.TYPE_INT_RGB)); //new BufferedReader dla BIzmienianyObszarRob
        obszar = new JPanel();
        obszar.setLayout(new BorderLayout());
        //obszar.setPreferredSize(new Dimension(480, 320));
        obszar.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
        obszarLabel = new JLabel(new ImageIcon(BIzmienianyObszarRob));
        obszar.add(obszarLabel);
        obszarLabel.addMouseMotionListener(new ObszarMML());
        obszarLabel.addMouseListener(new ObszarML());


        kontenerPrzybornik = new JToolBar();
        kontenerPrzyciskGora = new JPanel();
        wyczysc = new JButton("Wyczyść obszar roboczy");
        wyczysc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                wyczyscObszar(BIzmienianyObszarRob);
            }
        });
        kontenerPrzyciskiDol = new JPanel();
        zapis = new JButton("Zapisz do pliku");
        wczytywanie = new JButton("Wczytaj z pliku");

        kontenerPrzyciskGora.add(wyczysc);
        kontenerPrzyciskiDol.add(zapis);
        kontenerPrzyciskiDol.add(wczytywanie);

        przybornikLabel = new JLabel("PRZYBORNIK");
        przybornikLabel.setFont(new Font("Consolas", Font.BOLD, 17));
        przybornikLabel.setHorizontalAlignment(SwingConstants.CENTER);

        //kontenerPrzybornik.setLayout(new BoxLayout(kontenerPrzybornik, BoxLayout.Y_AXIS));


        kolorBT = new JButton("Kolor");
        kolorBT.setToolTipText("Zaznacz kolor");
        kolorBT.addActionListener(new KolorML());
        kolorBT.setIcon(new ImageIcon(probkaKoloru));
        kolor(kolor);

        rozmiarRysowaniaTryb = new SpinnerNumberModel(7,1,40,1);
        rozmiarRysowania = new JSpinner(rozmiarRysowaniaTryb);
        RozmiarRysCL rozmiarObsluga = new RozmiarRysCL();
        rozmiarRysowania.addChangeListener(rozmiarObsluga);
        rozmiarRysowania.setMaximumSize(rozmiarRysowania.getPreferredSize());

        rozmiarLabel = new JLabel("Rozmiar");
        rozmiarLabel.setLabelFor(rozmiarRysowania);
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(rozmiarLabel);
        toolBar.add(rozmiarRysowania);

        rysuj = new JRadioButton("Rysuj", true);
        pisz = new JRadioButton("Pisz", false);
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(rysuj);
        buttonGroup.add(pisz);

        JPanel jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
        //jp.add(toolBar);
        jp.add(rysuj);
        jp.add(pisz);


        JPanel jp2 = new JPanel(new BorderLayout());

        JPanel jp3 = new JPanel(new BorderLayout());
        jp3.add(przybornikLabel, BorderLayout.NORTH);
        jp3.add(toolBar, BorderLayout.CENTER);

        jp2.add(jp3, BorderLayout.NORTH);
        //jp2.add(rozmiarRysowania, BorderLayout.NORTH);
        jp2.add(jp, BorderLayout.CENTER);
        jp2.add(kolorBT, BorderLayout.SOUTH);
        kontenerPrzybornik.add(jp2);

        add(obszar, BorderLayout.CENTER);
        add(kontenerPrzybornik, BorderLayout.WEST);
        add(kontenerPrzyciskGora, BorderLayout.NORTH);
        add(kontenerPrzyciskiDol, BorderLayout.SOUTH);


        try{
            registry = LocateRegistry.getRegistry(nrPortu);
            rys = (Rysowanie) registry.lookup("RDraw");
        }catch (Exception e){e.printStackTrace();}
        this.setMinimumSize(this.getSize());


        setVisible(true);
    }

    public void ustawObszar(BufferedImage bi)
    {
        BIwyjsciowyObszarRob = bi;
        int wysokosc = bi.getHeight();
        int szerokosc = bi.getWidth();
        BIzmienianyObszarRob = new BufferedImage(1300,650,BufferedImage.TYPE_INT_ARGB);
        BufferedImage rmiPic = new BufferedImage(1300,650,BufferedImage.TYPE_INT_ARGB);
        try{
            registry = LocateRegistry.getRegistry(nrPortu);
            rys = (Rysowanie) registry.lookup("RDraw");
            ByteArrayInputStream bout = new ByteArrayInputStream(rys.setrmi());
            rmiPic = ImageIO.read(bout);
            BIzmienianyObszarRob = rmiPic;
        }catch (Exception e){e.printStackTrace();}

        if(obszarLabel !=null)
        {
            obszarLabel.setIcon(new ImageIcon(BIzmienianyObszarRob));
            obszarLabel.repaint();
        }

        if(obszar!=null)
        {
            //obszar.repaint();
            obszar.invalidate();
        }
    }

    /*
    class Obryss extends BasicStroke implements Serializable
    {
        protected static final long serialVersionUID = 1L;

        public Obryss(float width, int cap, int join, float miterlimit)
        {
            super(3,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,1.7f);
        }
    }*/

    private class KolorML implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            Color kolor = JColorChooser.showDialog(obszar,"Zaznacz kolor", Color.WHITE);
            if(kolor!= null)
            {
                kolor(kolor);
            }
            else
            {
                kolor(Color.WHITE);
            }
        }
    }

    private class ObszarMML extends MouseMotionAdapter {

        @Override
        public void mouseDragged(MouseEvent e) {
            if(aktywneNarzedzie==RysowanieClient.RYSOWANIE_NARZEDZIE)
            {
                rysowanie(e.getPoint());
            }
        }

    }

    private class ObszarML extends MouseAdapter
    {
        @Override
        public void mousePressed(MouseEvent e)
        {
            if(aktywneNarzedzie==RysowanieClient.RYSOWANIE_NARZEDZIE)
            {
                rysowanie(e.getPoint());
            }
            else if(aktywneNarzedzie==RysowanieClient.TEXT_NARZEDZIE)
            {
                pisanie(e.getPoint());
            }
        }
    }

    private class RozmiarRysCL implements ChangeListener
    {
        @Override
        public void stateChanged(ChangeEvent e)
        {
            Object obiekt = rozmiarRysowaniaTryb.getValue();
            Integer calkowi = (Integer) obiekt;
            obrys = new BasicStroke(calkowi.intValue(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.7f);
        }
    }

    public void kolor(Color kolor)
    {
        this.kolor = kolor;
        wyczysc(probkaKoloru);
    }

    public void rysowanie(Point w)
    {
        try{
            int xoff = 0;
            int yoff = 0;
            Point sloc = obszar.getLocationOnScreen();
            Point wspolrzedna = new Point(w.x-sloc.x-xoff,w.y-sloc.y-yoff);
            registry = LocateRegistry.getRegistry(nrPortu);
            rys = (Rysowanie)registry.lookup("RDraw");
            ByteArrayOutputStream pre = new ByteArrayOutputStream();
            ImageIO.write(BIzmienianyObszarRob,"jpg",pre);
            byte[] post;
            post = rys.rysujrmi(wspolrzedna, kolor, rozmiarRysowaniaTryb.getValue() ,cap_round, join_round, miterlimit);
            System.out.println(post.toString());
            ByteArrayInputStream postb = new ByteArrayInputStream(post);
            this.BIzmienianyObszarRob = ImageIO.read(postb);
            obszarLabel.setIcon(new ImageIcon(this.BIzmienianyObszarRob));
            obszarLabel.invalidate();

        }catch (Exception e){e.printStackTrace();}
    }

    public void pisanie(Point wspolrzedna)
    {
        String tresc = JOptionPane.showInputDialog(obszar, "Jaki tekst wstawic?", "Moj pierwszy rysunek");
        if (tresc!=null) {
            Graphics2D g = this.BIzmienianyObszarRob.createGraphics();
            g.setRenderingHints(rh);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(7,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,1.7f));
            g.drawString(tresc,wspolrzedna.x ,wspolrzedna.y);
            g.dispose();
        }
    }

    public void wyczysc(BufferedImage bufferedImage)
    {
        Graphics2D g = bufferedImage.createGraphics();
        g.setRenderingHints(rh);
        g.setColor(kolor);
        g.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        g.dispose();
        obszarLabel.repaint();
    }

    public void wyczyscObszar(BufferedImage bufferedImage)
    {
        try {
            registry = LocateRegistry.getRegistry(1099);
            rys = (Rysowanie)registry.lookup("RDraw");
            ByteArrayOutputStream pre = new ByteArrayOutputStream();
            ImageIO.write(BIzmienianyObszarRob,"jpg",pre);
            byte[] post;
            post = rys.wyczyscrmi();
            ByteArrayInputStream postb = new ByteArrayInputStream(post);
            this.BIzmienianyObszarRob = ImageIO.read(postb);
            obszarLabel.setIcon(new ImageIcon(this.BIzmienianyObszarRob));
            obszarLabel.invalidate();
        }catch (RemoteException e) {
            e.printStackTrace(); //blad w getRegistry()
        }catch (NotBoundException e){
            e.printStackTrace(); //blad w lookup()
        } catch (IOException e){
            e.printStackTrace(); //blad w write()/read()
        }
    }

    public static void main(String[] args) {
        new RysowanieClient(Integer.parseInt(args[0]));
    }
}
