import java.rmi.RemoteException;
/**
 * Created by Zdzislaw on 17.04.2017.
 */

//interfejs do serwera
public interface Rysowanie
{
    public void dodajKlienta() throws RemoteException;

    public void usunKlienta() throws RemoteException;
}
