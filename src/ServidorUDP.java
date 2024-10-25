import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServidorUDP {

    public static final int MIDA_PAQUET = 50;

    public static void main(String[] args) {
        if (args.length == 1) {
            try {
                // Creem el socket UDP per escoltar les peticions dels clients
                DatagramSocket s = new DatagramSocket(Integer.parseInt(args[0]));

                System.out.println("Servidor operatiu al port " + args[0] + "!");

                byte[] peticio = new byte[MIDA_PAQUET];
                DatagramPacket paquetPeticio = new DatagramPacket(peticio, peticio.length);

                while (true) {
                    System.out.println("Esperant petició d'algun client...");

                    // Rebem el paquet del client
                    s.receive(paquetPeticio);
                    System.out.println("Paquet rebut!");

                    // Convertim el paquet rebut en un número
                    String missatgeRebut = new String(paquetPeticio.getData(), 0, paquetPeticio.getLength()).trim();
                    int numero = Integer.parseInt(missatgeRebut);

                    System.out.println("Volen que multipliqui " + numero + " per 2...");

                    // Multipliquem el número per 2
                    int resultat = numero * 2;
                    String resposta = resultat + "\n";

                    // Preparem el paquet de resposta
                    byte[] respostaBytes = resposta.getBytes();
                    InetAddress adrecaClient = paquetPeticio.getAddress();
                    int portClient = paquetPeticio.getPort();
                    DatagramPacket paquetResposta = new DatagramPacket(respostaBytes, respostaBytes.length, adrecaClient, portClient);

                    // Enviem la resposta al client
                    s.send(paquetResposta);
                    System.out.println("Càlcul enviat!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("El nombre de paràmetres no és el correcte!");
        }
    }
}