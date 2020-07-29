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
	_ETHERNET_Frame ack_fame = new _ETHERNET_Frame();

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
	//상위계층의종류에따라서헤더에상위프로토콜타입을저장후물리적계층으로 Ethernet frame 전달 
	//① 상위계층으로부터 데이터를 전달받으면 그데이터를프레임의 데이터에 저장
	//② 수신될 Ethernet 주소와자신의 Ethernet 주소를헤더에 저장
	//③ Header type에 Data를보낸다는 type 설정 ex) 0x0001
	public boolean Send(byte[] input, int length) {

		if(input.length == length) {
			m_fame.enet_data=input;
			if(m_fame.enet_data.length > 1500) return false;
			//Header type에 Data를보낸다는 type 설정 ex) 0x0001
			m_fame.enet_type[0]=(byte)0x08;
			m_fame.enet_type[1]=(byte)0x01;
			byte[] frame = ObjToByteDATA(m_fame,input,length);
			this.GetUnderLayer().Send(frame,length+14);
		}
		else {
			//① ACK 프레임은 Ethernet Layer에서 데이터를 만듬 
			//② 수신될 Ethernet 주소와자신의 Ethernet 주소를헤더에 저장
			//③ Header type에 ACK를보낸다는 type 설정 ex) 0x0002
			if(length == 0) {
				m_fame.enet_type[0]=(byte)0x08;
				m_fame.enet_type[1]=(byte)0x02;

				byte[] ack_data = {'a','c','k'};
				//byte[] ack_data = new byte[3];
				byte[] frame = ObjToByteDATA(m_fame,ack_data,3);
				this.GetUnderLayer().Send(frame,frame.length);

			}else if(length ==1) {

				m_fame.enet_type[0]=(byte)0x08;
				m_fame.enet_type[1]=(byte)0x02;

				byte[] ack_data = {'a','c','k'};
				//byte[] ack_data = new byte[3];
				byte[] frame = ObjToByteDATA(m_fame,ack_data,3);
				for(int i=0;i<6;i++) {
					frame[i] = input[i];
				}
				this.GetUnderLayer().Send(frame,frame.length);
			}
		}
		return true;


	}

	//ReceiveACK
	//① 하위계층(physical layer)로부터 프레임을 받으면 type 확인후 Data, Ack인지확인 
	//② Ack type이면 프레임을 상위로올리지 않고상위계층 Send함수에게알림

	//① 하위계층(physical layer)로부터 프레임을 받으면 type 확인후 Data, Ack인지확인 
	//② Data type이면 상위로보내야하는지, 혹은폐기해야 하는지결정
	//③ 상위계층으로 보내는기준  
	//      목적지Ethernet 주소가브로드캐스트주소(ff-ff-ff-ff-ff-ff)일경우(type 확인해서수신) 
	//    목적지Ethernet 주소가자신의Ethernet 주소일경우(자기자신이보낸패킷X)
	public boolean Receive(byte[] input) {

		byte[] data;


		//목적지Ethernet 주소가자신의Ethernet 주소일경우(자기자신이보낸패킷X

		if(input[12]==(byte)0x08 && input[13]==(byte)0x01) {//Data수신


			if(srcyou_Addr(input) && dstme_Addr(input) && !srcme_Addr(input)) {
				data = RemoveCappHeader(input, input.length);
				if(data.length > 1500) return false;
				this.GetUpperLayer(0).Receive(data);
				Send(data,0);

				return true;
			}
			if(bro_Addr(input)&& !srcme_Addr(input)) {
				byte[] srcaddr = new byte[6];

				for(int i=0;i<6;i++) {
					srcaddr[i] = input[i+6];
				}
				data = RemoveCappHeader(input, input.length);
				if(data.length > 1500) return false;
				this.GetUpperLayer(0).Receive(data);
				Send(srcaddr,1);

				return true;
			}

		}else if(input[12]==(byte)0x08 && input[13]==(byte)0x02) {//ACK수신
			//System.out.println("ack");
			if(dstme_Addr(input)) {

				if(srcyou_Addr(input)) {

					data = RemoveCappHeader(input, input.length);
					//	System.out.println("ack data re: " + new String(input));
					this.GetUpperLayer(0).Send(data,0);
					//Send함수에게 알림
					return true;
				}

				//브로드캐스드로 보냄.
				//int me=0;
				for(int i=0 ; i<6 ; i++) {
					if(m_fame.enet_dstaddr.addr[i]!=(byte)0xff) return false;	
				}

				data = RemoveCappHeader(input, input.length);

				this.GetUpperLayer(0).Send(data,0);
				//Send함수에게 알림
				return true;
			}

		}
		return false;
	}

	public byte[] RemoveCappHeader(byte[] input, int length) {

		byte[] rebuf = new byte[length-14];
		for (int i = 0; i < length-14; i++) {

			rebuf[i] = input[14 + i];
		}
		return rebuf;
	}


	public boolean srcyou_Addr(byte[] add) {

		for(int i = 0;i<6;i++) {
			if(add[i+6]!=m_fame.enet_dstaddr.addr[i]) return false;
		}

		return true;
	}

	public boolean dstme_Addr(byte[] add) {

		for(int i = 0;i<6;i++) {
			if(add[i]!=m_fame.enet_srcaddr.addr[i]) return false;
		}

		return true;
	}
	public boolean srcme_Addr(byte[] add) {

		for(int i = 0;i<6;i++) {
			if(add[i+6]!=m_fame.enet_srcaddr.addr[i]) return false;
		}

		return true;
	}
	public boolean bro_Addr(byte[] add) {

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
