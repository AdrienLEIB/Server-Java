package server;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import javax.activation.MimetypesFileTypeMap;


public class server {
    // On stockera l'instance de ServerSocket ici pour plus de facilitÃ©
    private static ServerSocket serveur;
    // On stocke le port utilisÃ© en haut (pour Ã©viter de le chercher partout)
    private static int PORT = 8001;
    // Dossier ou se situe toutes les pages
	private static File www = new File("www");
	// index
	private static String index = "index.html";

    public server(){

    }
    
    public static void main(String[] args) throws IOException { 
    //on esssaye
	try {
		// le serveur et la connection restent allumé en boucle
		while(true) {
			// on lance le serveur 
			serveur = new  ServerSocket(PORT);
			
			
			System.out.println("En attente");
			//on attend une connection avec un client
			Socket socket = serveur.accept();
			  
			// liste qui sauvegarde le formulaire
			ArrayList<String> id= new ArrayList<>();
			ArrayList<String> value = new ArrayList<>();
			
			// on recupere la demande du client
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			// on crée le retour au client, ici ce sera le header
			PrintWriter out = new PrintWriter(socket.getOutputStream());
			// on crée le retour au client, ici ce sera la page en byte
			BufferedOutputStream byteOut = new BufferedOutputStream(socket.getOutputStream());
			
            //	ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            
            // on lit la requete du client
			String requete = in.readLine();
			// on decoupe la requete par des espace
			String[] link = requete.split(" ");
			// afin de recuperer la ligne du milieu qui est le chemin
			String path = link[1];
			
	
			// Dans le cas il y a un formulaire dans le chemin recupéré definit après un "?"
			int compteurInterogation = 0;
			for (int i=0;i<path.length();i++) {
				char c = path.charAt(i);
				if(c == '?')
				{
					compteurInterogation ++; 
				}
			}
			if(compteurInterogation>=1) {
				String[] morceaux = path.split("\\?");
				path = morceaux[0];
				String[] formulaire = morceaux[1].split("\\&");
				for(int i=0; i<formulaire.length; i++) {
					String[] seperateIdValue = formulaire[i].split("=");
					
					for(int j=0; j<seperateIdValue.length; j++) {
						if(j>0) {
							id.add(seperateIdValue[0]);
							value.add(seperateIdValue[1]);
						}
					}
				}
			}
			
			System.out.println("Liste des id :" + id);
			System.out.println("Liste des value :" + value);
			
			
			String pathAbsolute = www + "\\" + path;
			// le host
			InetAddress host = InetAddress.getLocalHost();
			
			// si le client ne speciefie pas de chemin on le redirige vers l'index
			if(path.endsWith("GET") || (path.endsWith("/"))){
				pathAbsolute = www+ "\\" +index;
			}
			
			File file = new File(pathAbsolute);
			// on recupere le content type du fichier
			String content = new MimetypesFileTypeMap().getContentType(file);
			// on essaye
			try {
				// si le chemin existe alors ça renvoie la page au client
				if(file.exists()) {
					// on transforme le contenu du fichier en byte
					byte[] encoded = Files.readAllBytes(Paths.get(pathAbsolute));
					// on envoie le header
					out.println("GET HTTP/1.1 200 OK");
					out.println("Host:" + host);
					out.println("Accept-Language: fr");
					out.println("Content-Length" + encoded.length );
					out.println("Content-type: " + content);	
				    out.println(); 
				    out.flush();
				    // on envoie la page
				    byteOut.write(encoded);	
				    byteOut.flush();
						      
				
				}
				// 404
				if(!file.exists()) {
					
					pathAbsolute = www + "\\404.html";
					file = new File(pathAbsolute);
					content = new MimetypesFileTypeMap().getContentType(file);
					System.out.println(pathAbsolute);
					byte[] encoded = Files.readAllBytes(Paths.get(pathAbsolute));
					out.println("HTTP/1.1 404 File Not Found");
					out.println("Host:" +host);
					out.println("Accept-Language: fr");
					out.println("Content-Length" + encoded.length );
					out.println("Content-type: " +content);	
				    out.println(); 
				    out.flush();
				    byteOut.write(encoded);	
				    byteOut.flush();
					      				
			}
				// si rien ne marche alors c'est une erreur 500
			}catch(Exception e) {
	            e.printStackTrace();            
	            pathAbsolute = www + "\\500.html";
	            file = new File(pathAbsolute);
				content = new MimetypesFileTypeMap().getContentType(file);
	            byte[] encoded = Files.readAllBytes(Paths.get(pathAbsolute));
	            out.println("HTTP/1.1 500 Internal Server Error");
				out.println("Host:" + host);
				out.println("Accept-Language: fr");
				out.println("Content-Length" + encoded.length );
				out.println("Content-type: " +content);	
			    out.println(); 
			    out.flush();
			    byteOut.write(encoded);	
			    byteOut.flush();	            
	        }
			// on ferme tous
		    in.close();
		    out.close();
		    byteOut.close();
		    socket.close();
		    serveur.close();
		}
    	}catch(Exception e) {
            e.printStackTrace();
            System.err.println("Erreur critque :(");
            serveur.close();
        }
        //serveur.close();

    }  	
}
