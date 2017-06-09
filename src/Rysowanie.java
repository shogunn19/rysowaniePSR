import java.awt.*;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created on 17.04.2017.
 */

public interface Rysowanie extends Remote
{
    public byte[] setrmi() throws RemoteException;
    public byte[] rysujrmi(Point p, Color c, Object rozmiarZSpinera, int capRound, int joinRound, float miterLimit) throws RemoteException;
    public byte[] piszrmi(String s, Point p, Color c, int capRound, int joinRound, float miterLimit) throws RemoteException;
    public byte[] wyczyscrmi() throws RemoteException;
    public byte[] odczytajrmi(byte[] input) throws RemoteException;
}
