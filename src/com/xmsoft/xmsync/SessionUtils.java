package com.xmsoft.xmsync;

import java.io.IOException;
import java.util.ArrayList;
import java.io.*;
import java.net.*;
import java.util.*;   
import org.apache.http.conn.util.InetAddressUtils;
public class SessionUtils {
	public static Socket connectToServer(Conexion conexion) throws IOException{
		Socket socket = new Socket(conexion.getHost(), conexion.getPort());
		MetadataList headers = new MetadataList();
		headers.add("USER", conexion.getUser());
		headers.add("PASS", conexion.getPass());
		OutputStream os = socket.getOutputStream();
		os.write(headers.toString().getBytes());
		os.flush();
		headers = extractMetadata(socket.getInputStream());
		if (headers.size()>0 && headers.get(0).getValue().equals("GOOD")){
			return socket;
		}
		return null;
	}
    public static  String encodeURI(String uri)
    {
        String ret = "";
        for (int i = 0; i < uri.length();i++)
        {
            char curChar = uri.charAt(i);
            if ((curChar < 48 || curChar > 57) &&
                (curChar < 65 || curChar > 90) &&
                (curChar < 97 || curChar > 122))
            {
                ret+= "%" + Integer.toHexString(curChar);
            }
			else
			{
				ret += curChar;
			}
        }
		return ret;
    }
	public static String decodeURI(String uri)
	{
		String ret = "";
		for (int i = 0; i < uri.length();i++)
		{
			char curChar = uri.charAt(i);
			if (curChar == '%')
			{
				String strCode = uri.substring(i+1, i+3);
				char ascii = (char)Integer.valueOf(strCode, 16).intValue();
				ret += ascii;
				i+=2;
			}
			else
			{
				ret += curChar;
			}
		}
		return ret;
	}
	
	public static class HeadAttrib{
		private String mName, mValue;
		public HeadAttrib(String name, int value){
			mName =  name;
			mValue = "" + value ;
		}
		public HeadAttrib(String name, String value, boolean isEncoded)
		{
			mName = name;
			mValue = isEncoded?value:encodeURI(value);
		}
		public HeadAttrib(String metadata, boolean isEncoded)
		{
		    int index = metadata.indexOf(" ");
		    mName = metadata.substring(0, index);
		    String value = metadata.substring(index + 1);
		    mValue = isEncoded?value:encodeURI(value);
		}
		public String getName()
		{
		    return mName;
		}
		public String getEncodedValue()
		{
		    return mValue;
		}
		public String getValue()
		{
		    return decodeURI(mValue);
		}
		public void setValue(int value){
			mValue = value + "";
		}
		public void setValue(String value){
			mValue = encodeURI(value);
		}
		public void setEncodedValue(String value){
			mValue = value;
		}
		public int getIntValue()
		{
		    return Integer.parseInt(mValue);
		}
		public String toString(){
			return toString(true);
		}
		public String toString(boolean encoded)
		{
			if (encoded)
		    	return mName + " " + mValue;
			return mName + " " + decodeURI(mValue);
		}

		public HeadAttrib clone(){
			return new HeadAttrib(mName, mValue,true);
		}
	}


	public static class MetadataList extends ArrayList<HeadAttrib>
    {
		private static final long serialVersionUID = 1L;
		public HeadAttrib getAttribByName(String name)
        {
            for (int i = 0; i < size(); i++)
            {
                HeadAttrib attrib = get(i);
                if (attrib.getName().equalsIgnoreCase(name))
                    return attrib;
            }
            return null;
        }
        
        public String toString()
        {
            String ret = "";
            for (int i = 0; i < size(); i++)
            {
                HeadAttrib attrib = get(i);
                ret += attrib.toString() + "\n";
            }
            ret += "\n";
            return ret;
        }
        public boolean add(String name, int value)
        {
            return add(new HeadAttrib(name, value));
        }
        public boolean add(String name, String unCodedValue)
        {
            return add(new HeadAttrib(name, unCodedValue, false));
        }
    };
    
    public static MetadataList extractMetadata(InputStream receiver) throws IOException
    {
        MetadataList metadata = new MetadataList();
       	StringBuilder content = new StringBuilder(500);
        int lastPos = 0;
        int lineCount = 0;

        while (lineCount != 2)
        {
            char tmp;
            tmp =(char) receiver.read();
            content.append(tmp);
            if (tmp == '\n')
            {
                lineCount++;
            }
            else{
                lineCount = 0;
            }
        }

        for (int i = 0; i < content.length();i++)
        {
            if (content.charAt(i) == '\n' && i>lastPos)
            {
            	String linea = content.substring(lastPos, i);
                HeadAttrib attrib = new HeadAttrib(linea, true);
                metadata.add(attrib);
                lastPos = i + 1;
            }
        }
        return metadata;
    }


    /**
     * Get IP address from first non-localhost interface
     * @param ipv4  true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr); 
                        if (useIPv4) {
                            if (isIPv4) 
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                return delim<0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }

}


