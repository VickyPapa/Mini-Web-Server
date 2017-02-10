import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;

public class MiniWebServer {
    private static int Port = 3541; //�˿ں�
    private static String ServerPrePath = "C:/Users/Administrator/Documents/MiniWebServer/"; //����������Ŀ¼��ӳ����Ϊ�ļ�����·����ǰ׺
	
    public static String getServerPrePath(){
    	return ServerPrePath;
    }
    
    public static void main(String[] args) {
    	//���������׽���
        ServerSocket server = null;
        Socket client = null;
        try{
            server = new ServerSocket(Port);
            //��������ʼ����
            System.out.println("��ʼ�����˿�"+server.getLocalPort());
            while(true){
            	client = server.accept();
            	//���߳����У�ÿ��һ�����ӽ���ʱ���������ֳ�һ��ͨ�ŵ��߳�
            	new HandleThread(client).start();
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
 
}

class HandleThread extends Thread{
    Socket client; //��ͻ���ͨ�ŵ��׽���
    
    public HandleThread(Socket s) {
        client = s;
    }
 
    public void sendFile(PrintStream out,File file){
        try{
            DataInputStream in  = new DataInputStream(new FileInputStream(file));
            int len = (int)file.length();
            byte buf[] = new byte[len];
            in.readFully(buf);//��ȡ�����ݵ�buf������
            out.write(buf,0,len);
            out.flush();
            in.close();
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
     
    public void run(){
        try{
            //�������������
            PrintStream out = new PrintStream(client.getOutputStream());
            //��������������
            DataInputStream in = new DataInputStream(client.getInputStream());
            //��ȡ������ύ������
            String msg = in.readLine();
             
            if(!msg.contains("dopost")){ //�����GET
                System.out.println("GET");
                //��ȡ�ļ�·��
                String fileName = msg.substring(msg.indexOf(' ')+1);
                fileName = fileName.substring(1,fileName.indexOf(' '));
                fileName = MiniWebServer.getServerPrePath()+fileName;
                System.out.println("The user asked for resource: "+fileName);
                File file = new File(fileName);                
                if(file.exists()){                  
                    //������Ӧ���ĸ�ʽ����
                    System.out.println(fileName+" start send");
                     
                    out.println("HTTP/1.0 200 OK"); 
                    out.println("MIME_version:1.0");
                    out.println("Content_Type:text/html");
                    int len = (int) file.length();
                    out.println("Content_Length:"+len);
                    out.println("");//����ͷ����Ϣ֮��Ҫ��һ��                    
                    //�����ļ�
                    sendFile(out,file);                   
                    out.flush();
                } 
                else{ //����ļ������ڣ���Ӧ��Ϣ��״̬����Ϊ404
                    out.println("HTTP/1.1 404 File Not Found");
                    out.println("MIME_version:1.0");
                    out.println("Content_Type:text/html");
                    String response="<html><body>"+"File Not Found"+"</body></html>";
                    int len = (int) response.length();
                    out.println("Content_Length:"+len);
                    out.println("");
                    out.println(response);
                }
            }
            else { //�����DOPOST
                System.out.println("DOPOST");
                //��ȡ��login��pass��ֵ
                String login = msg.substring(msg.indexOf("login="));
                login = login.substring(6,login.indexOf('&'));
                String pass= msg.substring(msg.indexOf("pass="));
                pass = pass.substring(5,pass.indexOf(' '));
                String response=null;
                //����login��pass��ֵ
                if(login.equals("3140103541")&&pass.equals("3541")){
                    response="<html><body>"+"��¼�ɹ�"+"</body></html>";
                }
                else{
                    response="<html><body>"+"��¼ʧ��"+"</body></html>";
                }
                //������Ӧ���ĸ�ʽ����                
                out.println("HTTP/1.0 200 OK"); 
                out.println("MIME_version:1.0");
                out.println("Content_Type:text/html");
                int len = (int) response.length();
                out.println("Content_Length:"+len);
                out.println("");//����ͷ����Ϣ֮��Ҫ��һ��                    
                //������Ӧ��Ϣ
                out.println(response);                   
                out.flush();
            }             
            
            client.close();     
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }       
    }
}
