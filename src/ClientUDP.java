import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class ClientUDP {

    public static final int MIDA_PAQUET = 50;

    public static void main(String[] args) {
        if (args.length == 2) {
            try {
                // Creem el socket UDP per enviar i rebre missatges del servidor
                DatagramSocket s = new DatagramSocket();

                // Preparem l'adreça i el port del servidor
                InetAddress adrecaServidor = InetAddress.getByName(args[0]);
                int portServidor = Integer.parseInt(args[1]);

                // Obtenim el número del teclat
                Scanner scanner = new Scanner(System.in);
                System.out.println("Benvingut/da!\nPosa un número:");
                int numero = scanner.nextInt();
                scanner.close();

                // Muntem el paquet a enviar
                String peticio = numero + "\n";
                byte[] peticioBytes = peticio.getBytes();
                DatagramPacket paquetPeticio = new DatagramPacket(peticioBytes, peticioBytes.length, adrecaServidor, portServidor);

                // Enviem el paquet al servidor
                s.send(paquetPeticio);
                System.out.println("Paquet enviat! Espero resposta...");

                // Preparem per rebre la resposta del servidor
                byte[] buffer = new byte[MIDA_PAQUET];
                DatagramPacket paquetResposta = new DatagramPacket(buffer, buffer.length);

                // Rebem la resposta del servidor
                s.receive(paquetResposta);
                String resposta = new String(paquetResposta.getData(), 0, paquetResposta.getLength()).trim();

                // Mostrem la resposta del servidor
                System.out.println("He rebut la resposta: " + resposta);

                // Tanquem el socket
                s.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("El nombre de paràmetres no és el correcte!");
        }
    }
}