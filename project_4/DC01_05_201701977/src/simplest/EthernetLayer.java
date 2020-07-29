package simplest;

import java.util.ArrayList;


// Ethernet 프레임에서최대전송단위(MTU) : 1500bytes (Ethernet 헤더제외)
public class EthernetLayer implements BaseLayer  {

	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	public static int upperLayer_num ;


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


	public byte[] ObjToByteDATA(_ETHERNET_Frame Header, byte[] input, int length) {
		byte[] buf = new byte[length + 14];

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


	public boolean Send(byte[] input, int length, Object ob) {

		//		 상위계층의종류에따라서헤더에상위프로토콜형태저장후물리적계층으로 Ethernet frame 전달(enet_type) 
		//		§ 0x2080: ChattingAppLayer 
		//		§ 0x2090: FileAppLayer 

		m_fame.enet_data=input;
		if(m_fame.enet_data.length > 1500) return false;

		if(ob == this.GetUpperLayer(0).GetLayerName()) {
			m_fame.enet_type[0]=(byte)0x20;
			m_fame.enet_type[1]=(byte)0x80;		

			byte[] frame = ObjToByteDATA(m_fame,input,length);
			GetUnderLayer().Send(frame,length+14);

		}else if(ob == this.GetUpperLayer(1).GetLayerName()){
			
			m_fame.enet_type[0]=(byte)0x20;
			m_fame.enet_type[1]=(byte)0x90;		
			
			byte[] frame = ObjToByteDATA(m_fame,input,length);
			GetUnderLayer().Send(frame,length+14);

		}

		return true;

	}

	
	public boolean Receive(byte[] input) {

		byte[] data;


		if(input[12]==(byte)0x20 && input[13]==(byte)0x80) {//ChatData수신
			data = RemoveCappHeader(input, input.length);
			if(data.length > 1500) return false;

			if(srcyou_Addr(input) && dstme_Addr(input)) {//주소확인
			
				this.GetUpperLayer(0).Receive(data);

				return true;
			}
			
			if(bro_Addr(input)&& !srcme_Addr(input)) {//주소확인 내가 보낸 브로드캐스트는 받지않는다.

				
				this.GetUpperLayer(0).Receive(data);

				return true;
			}

		}else if(input[12]==(byte)0x20 && input[13]==(byte)0x90) {//FileData수신

			data = RemoveCappHeader(input, input.length);
			if(data.length > 1500) return false;
			
			if(srcyou_Addr(input) && dstme_Addr(input)) {  //주소확인

				this.GetUpperLayer(1).Receive(data);

				return true;
			}
			if(bro_Addr(input)&& !srcme_Addr(input)) {//주소확인
	
				this.GetUpperLayer(1).Receive(data);
			

				return true;
			}

		}

		return false;
	}

	public byte[] RemoveCappHeader(byte[] input, int length) {

		byte[] rebuf = new byte[length-14];
		m_fame.enet_data = new byte[length-14];
		
		for (int i = 0; i < length-14; i++) {
			
			m_fame.enet_data[i] = input[14 + i];
			rebuf[i] = input[14 + i];
		}
		return rebuf;
	}


	public boolean srcyou_Addr(byte[] add) {//주소확인

		for(int i = 0;i<6;i++) {
			if(add[i+6]!=m_fame.enet_dstaddr.addr[i]) return false;
		}

		return true;
	}

	public boolean dstme_Addr(byte[] add) {//주소확인

		for(int i = 0;i<6;i++) {
			if(add[i]!=m_fame.enet_srcaddr.addr[i]) return false;
		}

		return true;
	}
	public boolean srcme_Addr(byte[] add) {//주소확인

		for(int i = 0;i<6;i++) {
			if(add[i+6]!=m_fame.enet_srcaddr.addr[i]) return false;
		}

		return true;
	}
	public boolean bro_Addr(byte[] add) {//주소확인

		for(int i = 0;i<6;i++) {
			if(add[i]!=(byte)0xff) return false;
		}
		return true;
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
