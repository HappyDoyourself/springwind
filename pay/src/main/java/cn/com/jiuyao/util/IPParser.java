package cn.com.jiuyao.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class IPParser {
	private String DbPath = ""; // 纯真IP数据库地址

	private String Country, LocalStr;

	private long IPN;

	private int RecordCount, CountryFlag;

	private long RangE, RangB, OffSet, StartIP, EndIP, FirstStartIP,
			LastStartIP, EndIPOff;

	private RandomAccessFile fis;

	private byte[] buff;

	public IPParser() {
		super();
		try {
			this.DbPath = java.net.URLDecoder.decode(IPParser.class.getResource("/ip/qqwry.dat").getPath(),"gbk");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			// TODO Auto-generated catch block
	}
	
	private long B2L(byte[] b) {
		long ret = 0;
		for (int i = 0; i < b.length; i++) {
			long t = 1L;
			for (int j = 0; j < i; j++)
				t = t * 256L;
			ret += ((b[i] < 0) ? 256 + b[i] : b[i]) * t;
		}
		return ret;
	}

	private long ipToInt(String ip) {
		String[] arr = ip.split("\\.");
		long ret = 0;
		for (int i = 0; i < arr.length; i++) {
			long l = 1;
			for (int j = 0; j < i; j++)
				l *= 256;
			try {
				ret += Long.parseLong(arr[arr.length - i - 1]) * l;
			} catch (Exception e) {
				ret += 0;
			}
		}
		return ret;
	}

	public void seek(String ip) throws Exception {
		this.IPN = ipToInt(ip);
		fis = new RandomAccessFile(this.DbPath, "r");
		buff = new byte[4];
		fis.seek(0);
		fis.read(buff);
		FirstStartIP = this.B2L(buff);
		fis.read(buff);
		LastStartIP = this.B2L(buff);
		RecordCount = (int) ((LastStartIP - FirstStartIP) / 7);
		if (RecordCount <= 1) {
			LocalStr = Country = "未知";
			throw new Exception();
		}

		RangB = 0;
		RangE = RecordCount;
		long RecNo;

		do {
			RecNo = (RangB + RangE) / 2;
			getStartIP(RecNo);
			if (IPN == StartIP) {
				RangB = RecNo;
				break;
			}
			if (IPN > StartIP)
				RangB = RecNo;
			else
				RangE = RecNo;
		} while (RangB < RangE - 1);

		getStartIP(RangB);
		getEndIP();
		getCountry(IPN);

		fis.close();
	}

	private String getFlagStr(long OffSet) throws IOException {
		int flag = 0;
		do {
			fis.seek(OffSet);
			buff = new byte[1];
			fis.read(buff);
			flag = (buff[0] < 0) ? 256 + buff[0] : buff[0];
			if (flag == 1 || flag == 2) {
				buff = new byte[3];
				fis.read(buff);
				if (flag == 2) {
					CountryFlag = 2;
					EndIPOff = OffSet - 4;
				}
				OffSet = this.B2L(buff);
			} else
				break;
		} while (true);

		if (OffSet < 12) {
			return "";
		} else {
			fis.seek(OffSet);
			return getStr();
		}
	}

	private String getStr() throws IOException {
		long l = fis.length();
		ByteArrayOutputStream byteout = new ByteArrayOutputStream();
		byte c = fis.readByte();
		do {
			byteout.write(c);
			c = fis.readByte();
		} while (c != 0 && fis.getFilePointer() < l);
		String gbk = new String(byteout.toByteArray(),"GBK");
		String iso = new String(gbk.getBytes("UTF-8"),"ISO-8859-1");
		return new String(iso.getBytes("ISO-8859-1"),"UTF-8");
	}

	private void getCountry(long ip) throws IOException {
		if (CountryFlag == 1 || CountryFlag == 2) {
			Country = getFlagStr(EndIPOff + 4);
			if (CountryFlag == 1) {
				LocalStr = getFlagStr(fis.getFilePointer());
				if (IPN >= ipToInt("255.255.255.0")
						&& IPN <= ipToInt("255.255.255.255")) {
					LocalStr = getFlagStr(EndIPOff + 21);
					Country = getFlagStr(EndIPOff + 12);
				}
			} else {
				LocalStr = getFlagStr(EndIPOff + 8);
			}
		} else {
			Country = getFlagStr(EndIPOff + 4);
			LocalStr = getFlagStr(fis.getFilePointer());
		}
	}

	private long getEndIP() throws IOException {
		fis.seek(EndIPOff);
		buff = new byte[4];
		fis.read(buff);
		EndIP = this.B2L(buff);
		buff = new byte[1];
		fis.read(buff);
		CountryFlag = (buff[0] < 0) ? 256 + buff[0] : buff[0];
		return EndIP;
	}

	private long getStartIP(long RecNo) throws IOException {
		OffSet = FirstStartIP + RecNo * 7;
		fis.seek(OffSet);
		buff = new byte[4];
		fis.read(buff);
		StartIP = this.B2L(buff);
		buff = new byte[3];
		fis.read(buff);
		EndIPOff = this.B2L(buff);
		return StartIP;
	}

	public String getLocal() {
		return this.LocalStr;
	}

	public String getCountry() {
		return this.Country;
	}

	public void setPath(String path) {
		this.DbPath = path;
	}

	public static void main(String[] args) throws Exception {
		
		long initUsedMemory = ( Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ) ;
		
		long start = System.currentTimeMillis();
		
		IPParser w = new IPParser();
		// w.setPath(new File("QQWry2.Dat").getAbsolutePath());
		w.seek("125.10.100.17");
		System.out.println(w.getCountry() + " " + w.getLocal());
		long end = System.currentTimeMillis();
		
		long endUsedMemory = ( Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ) ;
		
		System.out.println("time spent:"+ ( end - start ) + " ns" );
		System.out.println("memory consumes:" + ( endUsedMemory - initUsedMemory ) );
		
	}

}