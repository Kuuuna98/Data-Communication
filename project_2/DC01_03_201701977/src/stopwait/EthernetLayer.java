package stopwait;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;



// Ethernet 프레임에서최대전송단위(MTU) : 1500bytes (Ethernet 헤더제외)
public class EthernetLayer implements BaseLayer  {
	
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	public EthernetLayer(String pName) { 
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName; 
	
	}

	//ethernet address
	private class _ETHERNET_ADDR{
		private byte[] addr = new byte[6];
		
		public _ETHERNET_ADDR() {
			this.addr[0] = (byte) 0x00;
			this.addr[1] = (byte) 0x00;
			this.addr[2] = (byte) 0x00;
			this.addr[3] = (byte) 0x00;
			this.addr[4] = (byte) 0x00;
			this.addr[5] = (byte) 0x00;
		}
	}

	
	//ethernet protocol
	private class _ETHERNET_Frame{
		_ETHERNET_ADDR enet_dstaddr;
		_ETHERNET_ADDR enet_srcaddr;
		byte[] enet_type;
		byte[] enet_data;
		
		public _ETHERNET_Frame(){
			this.enet_dstaddr = new _ETHERNET_ADDR() ;
			this.enet_srcaddr = new _ETHERNET_ADDR();
			this.enet_type = new byte[2];
			this.enet_data = null;
		}
	
	}
	
	_ETHERNET_Frame m_fame = new _ETHERNET_Frame();
	
	public void SetEnetSrcAddress(byte[] srcAddress) {
		// TODO Auto-generated method stub
		m_fame.enet_srcaddr.addr[0]= srcAddress[0];
		m_fame.enet_srcaddr.addr[1]= srcAddress[1];
		m_fame.enet_srcaddr.addr[2]= srcAddress[2];
		m_fame.enet_srcaddr.addr[3]= srcAddress[3];
		m_fame.enet_srcaddr.addr[4]= srcAddress[4];
		m_fame.enet_srcaddr.addr[5]= srcAddress[5];

	}

	public void SetEnetDstAddress(byte[] dstAddress) {
		// TODO Auto-generated method stub
		m_fame.enet_dstaddr.addr[0] = dstAddress[0];
		m_fame.enet_dstaddr.addr[1] = dstAddress[1];
		m_fame.enet_dstaddr.addr[2] = dstAddress[2];
		m_fame.enet_dstaddr.addr[3] = dstAddress[3];
		m_fame.enet_dstaddr.addr[4] = dstAddress[4];
		m_fame.enet_dstaddr.addr[5] = dstAddress[5];
		
	}
	
	public byte[] ObjToByte(_ETHERNET_Frame Header, byte[] input, int length) {
		byte[] buf = new byte[length + 14];
		Header.enet_type[0]=(byte)0x08;
		Header.enet_type[1]=(byte)0x06;
		
		buf[0] = Header.enet_dstaddr.addr[0];
		buf[1] = Header.enet_dstaddr.addr[1];
		buf[2] = Header.enet_dstaddr.addr[2];
		buf[3] = Header.enet_dstaddr.addr[3];
		buf[4] = Header.enet_dstaddr.addr[4];
		buf[5] = Header.enet_dstaddr.addr[5];
		buf[6] = Header.enet_srcaddr.addr[0];
		buf[7] = Header.enet_srcaddr.addr[1];
		buf[8] = Header.enet_srcaddr.addr[2];
		buf[9] = Header.enet_srcaddr.addr[3];
		buf[10] = Header.enet_srcaddr.addr[4];
		buf[11] = Header.enet_srcaddr.addr[5];
		buf[12] = Header.enet_type[0];
		buf[13] = Header.enet_type[1];
		for (int i = 0; i < length; i++) {
			buf[14 + i] = input[i];
			
		}
	

		return buf;
	}
	//상위계층의종류에따라서헤더에상위프로토콜타입을저장후물리적계층으로 Ethernet frame 전달 
	public boolean Send(byte[] input, int length) {
		m_fame.enet_data=input;
		if(m_fame.enet_data.length > 1500) return false;
		byte[] frame = ObjToByte(m_fame,input,length);
		this.GetUnderLayer().Send(frame,length+14);
		return true;
	}

	public boolean Receive(byte[] input) {
		
		byte[] data;
		int bro =0;
		int me =0;
		int you =0;

		for(int i = 0;i<6;i++) {
			if(m_fame.enet_srcaddr.addr[i] == input[i])  me++;
			if(input[i] == (byte)0xFF) bro++;
			if(input[i+6]==m_fame.enet_dstaddr.addr[i]) you++;
		}	
			
		if((me != 6 || you!=6) && (bro != 6  ||(input[12]!=(byte)0x08) || (input[13]!=(byte)0x06) )) return false;
		data = RemoveCappHeader(input, input.length);
		this.GetUpperLayer(0).Receive(data);
	
		return true;

	}
	
	public byte[] RemoveCappHeader(byte[] input, int length) {
		
		byte[] rebuf = new byte[length-14];
		for (int i = 0; i < length-14; i++) {
			
			rebuf[i] = input[14 + i];
		}
		return rebuf;
	}
	
	
	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		// TODO Auto-generated method stub
		if (pUnderLayer == null)
			return;
		p_UnderLayer = pUnderLayer;
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		// TODO Auto-generated method stub
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
		// nUpperLayerCount++;
	}

	@Override
	public String GetLayerName() {
		// TODO Auto-generated method stub
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		if (p_UnderLayer == null)
			return null;
		return p_UnderLayer;
	}

	@Override
	public BaseLayer GetUpperLayer(int nindex) {
		// TODO Auto-generated method stub
		if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			return null;
		return p_aUpperLayer.get(nindex);
	}

	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);

	}

}
