
import java.io.*;
import java.io.InputStreamReader;
import java.net.Socket;

public final class PhysLayerClient {

    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("codebank.xyz", 38002)) {
            System.out.println("Connected to server.");
            
            //Initialize an input stream and an output stream to communicate with server
            InputStream is = socket.getInputStream();

            OutputStream os = socket.getOutputStream();
            PrintStream out = new PrintStream(os, true, "UTF-8");

            //holds 64 signals that will be used to calculate the preamble
            int[] preamble = new int[64];

            //holds the signals for the 32 byte message
            int[] mess = new int[320];

            int j = 0;
            int counter = 0;
            int q = 0;
            int k;

            //Reads the bytes from the server and stores them in arrays
            while((k = is.read()) != -1){
              
              if(counter < 64){
                 preamble[q] = k; 
                 q++;
              }else if(counter > 63){
                 mess[j] = k;
                 j++;              
              }

              counter++;
              if(counter > 383)
              break;

            }

            //Calculates the preamble
            float sumA = 0;
            for(int i = 0; i < preamble.length ; i++){
               sumA += preamble[i];
            }

            float preA = sumA/64; 
            String str = String.format("%.02f", preA);
            System.out.println("Baseline established from preamble: " + str);                             

            //Figures out the 5B representation of the message
            String m5B = "";
            int i = 0;
            int bSignal = mess[i];

            if(bSignal > preA)
               m5B += "1";
            else 
               m5B += "0";
                
            for(i = 1; i < 320; i++){
              if(mess[i] > preA){
                if(bSignal > preA)
                    m5B += "0";
                else
                    m5B += "1";
              }
              else if(mess[i] < preA){
                if(bSignal < preA)
                    m5B += "0";
                else
                    m5B += "1";               
              }
              bSignal = mess[i];
            } 

            //Figures out the 4B representation of the message
            String m4B = "";
            int z = 0; 
            int x = 5;
            for(int l = 0; l < 64; l++){

              String sect = m5B.substring(z,x);

              if(sect.equals("11110"))
                 m4B += "0000";
              else if(sect.equals("01001"))
                 m4B += "0001";
              else if(sect.equals("10100"))
                 m4B += "0010";
              else if(sect.equals("10101"))
                 m4B += "0011";
              else if(sect.equals("01010"))
                 m4B += "0100";
              else if(sect.equals("01011"))
                 m4B += "0101";
              else if(sect.equals("01110"))
                 m4B += "0110";
              else if(sect.equals("01111"))
                 m4B += "0111";
              else if(sect.equals("10010"))
                 m4B += "1000";
              else if(sect.equals("10011"))
                 m4B += "1001";
              else if(sect.equals("10110"))
                 m4B += "1010";
              else if(sect.equals("10111"))
                 m4B += "1011";
              else if(sect.equals("11010"))
                 m4B += "1100";
              else if(sect.equals("11011"))
                 m4B += "1101";
              else if(sect.equals("11100"))
                 m4B += "1110";
              else if(sect.equals("11101"))
                 m4B += "1111";
                
              z += 5;
              x += 5;
            }

            //Turns the 256 bit message into 32 bytes
            z = 0;
            x = 8;
            byte[] Bmess = new byte[32];
            System.out.print("Received 32 bytes: " );
            for(int l = 0; l < 32; l++){
              Bmess[l] = (byte) Integer.parseInt(m4B.substring(z,x), 2);
              System.out.print(Integer.toHexString(Bmess[l] & 0XFF).toUpperCase());

              z += 8;
              x += 8;
            }
            
            System.out.println();
            
            //Send 32 byte message to server
            out.write(Bmess);

            //Read response from server
            int res = (int)is.read();

            //Determines whether or not the correct 32 byte message was sent
            if(res == 1)
            System.out.println("Response good.");
            else
            System.out.println("Response bad.");

            System.out.println("Disconnected from server.");
      }
    }
    
}















