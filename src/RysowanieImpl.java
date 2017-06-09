import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 17.04.2017.
 */

class RysowanieImpl extends UnicastRemoteObject implements Rysowanie
{
    private static BufferedImage common;
    private Map<RenderingHints.Key, Object> hm;
    private RenderingHints rh;

    //private JPanel obszarJP;

    public RysowanieImpl() throws RemoteException
    {
        super();
        hm = new HashMap<RenderingHints.Key,Object>();
        hm.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hm.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        hm.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        rh = new RenderingHints(hm);
        common = new BufferedImage(1300,650,BufferedImage.TYPE_INT_ARGB);

        //obszarJP = new JPanel();

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