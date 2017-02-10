import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;

public class MiniWebServer {
    private static int Port = 3541; //端口号
    private static String ServerPrePath = "C:/Users/Administrator/Documents/MiniWebServer/"; //服务器部署目录，映射作为文件绝对路径的前缀
	
    public static String getServerPrePath(){
    	return ServerPrePath;
    }
    
    public static void main(String[] args) {
    	//创建两个套接字
        ServerSocket server = null;
        Socket client = null;
        try{
            server = new ServerSocket(Port);
            //服务器开始监听
            System.out.println("开始监听端口"+server.getLocalPort());
            while(true){
            	client = server.accept();
            	//多线程运行，每有一个连接建立时，服务器分出一个通信的线程
            	new HandleThread(client).start();
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
 
}

class HandleThread extends Thread{
    Socket client; //与客户端通信的套接字
    
    public HandleThread(Socket s) {
        client = s;
    }
 
    public void sendFile(PrintStream out,File file){
        try{
            DataInputStream in  = new DataInputStream(new FileInputStream(file));
            int len = (int)file.length();
            byte buf[] = new byte[len];
            in.readFully(buf);//读取文内容到buf数组中
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
            //创建输出流对象
            PrintStream out = new PrintStream(client.getOutputStream());
            //创建输入流对象
            DataInputStream in = new DataInputStream(client.getInputStream());
            //读取浏览器提交的请求
            String msg = in.readLine();
             
            if(!msg.contains("dopost")){ //如果是GET
                System.out.println("GET");
                //获取文件路径
                String fileName = msg.substring(msg.indexOf(' ')+1);
                fileName = fileName.substring(1,fileName.indexOf(' '));
                fileName = MiniWebServer.getServerPrePath()+fileName;
                System.out.println("The user asked for resource: "+fileName);
                File file = new File(fileName);                
                if(file.exists()){                  
                    //根据响应报文格式设置
                    System.out.println(fileName+" start send");
                     
                    out.println("HTTP/1.0 200 OK"); 
                    out.println("MIME_version:1.0");
                    out.println("Content_Type:text/html");
                    int len = (int) file.length();
                    out.println("Content_Length:"+len);
                    out.println("");//报文头和信息之间要空一行                    
                    //发送文件
                    sendFile(out,file);                   
                    out.flush();
                } 
                else{ //如果文件不存在，响应消息的状态设置为404
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
            else { //如果是DOPOST
                System.out.println("DOPOST");
                //提取出login和pass的值
                String login = msg.substring(msg.indexOf("login="));
                login = login.substring(6,login.indexOf('&'));
                String pass= msg.substring(msg.indexOf("pass="));
                pass = pass.substring(5,pass.indexOf(' '));
                String response=null;
                //检验login和pass的值
                if(login.equals("3140103541")&&pass.equals("3541")){
                    response="<html><body>"+"登录成功"+"</body></html>";
                }
                else{
                    response="<html><body>"+"登录失败"+"</body></html>";
                }
                //根据响应报文格式设置                
                out.println("HTTP/1.0 200 OK"); 
                out.println("MIME_version:1.0");
                out.println("Content_Type:text/html");
                int len = (int) response.length();
                out.println("Content_Length:"+len);
                out.println("");//报文头和信息之间要空一行                    
                //发送响应消息
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
