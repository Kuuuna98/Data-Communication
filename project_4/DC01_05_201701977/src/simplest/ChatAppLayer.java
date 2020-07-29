package simplest;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.swing.SingleSelectionModel;

import simplest.FileAppLayer.Send_Thread;

public class ChatAppLayer implements BaseLayer {


	private Send_Thread thread;


	//chat application protocol
	private class _CAPP_APP {

		byte capp_unused; 
		byte capp_type; //단편화에 대한 정보를 담을 수 있다.
		//0x00 – 단편화되지않음, 0x01 – 단편화첫부분, 0x02 – 단편화중간, 0x03- 단 편화마지막
		byte[] capp_totlen; //사용자가 입력한 문자열의 길이를 저장
		byte[] capp_data;


		public _CAPP_APP() {
			this.capp_type = 0x00;
			this.capp_unused = 0x00;
			this.capp_totlen = new byte[2];
			this.capp_data = null;
		}
	}

	_CAPP_APP m_sHeader = new _CAPP_APP();

	
	private int wait=0;

	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	private ByteBuffer chatBuf;


	public ChatAppLayer(String pName) { 
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName; 

		//	EthernetLayer.thread_layer(0);
	}

	class Send_Thread implements Runnable{ //패킷수신을위한Runnable클래스
		//Pcap처리에필요한네트워크어뎁터및상위레이어객체초기화
		byte[] input;
		int length;
		BaseLayer UnderLayer;
		public Send_Thread(byte[] input,  int length,BaseLayer m_UnderLayer) {
			this.input = input;
			this.length = length;
			UnderLayer = m_UnderLayer;
		}

		@Override
		public void run() {
			//	while(input.length == length && wait !=0 ) {
			while(wait !=0) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) { }
			}

			wait = 1;

			m_sHeader.capp_totlen[1] =(byte) ((length >> 8 ) & 0xff);
			m_sHeader.capp_totlen[0] =(byte) (length & 0xff);


			if(length > 1456) {
				byte[] talk = input;
				byte[] inputCut = new byte[1456];
				int lengthCut=0;
				
				m_sHeader.capp_type=(byte)0x01; 
				
				for(int i=0;i<1456;i++) {
					inputCut[i] = talk[i];
				}
				
				lengthCut = lengthCut+1456;
				
				byte[] bytes = ObjToByte(m_sHeader,inputCut, inputCut.length);
				UnderLayer.Send(bytes,bytes.length,pLayerName);

				try {
					Thread.sleep(1);
				} catch (InterruptedException e) { }
				
				while(talk.length - lengthCut > 1456) {
					m_sHeader.capp_type=(byte)0x02;
					inputCut = new byte[1456];
					for(int i=0;i<1456;i++) {
						inputCut[i] = talk[i+lengthCut];
					}
					//System.out.println("2: "+new String(inputCut));
					lengthCut = lengthCut+1456;
					bytes = ObjToByte(m_sHeader,inputCut,inputCut.length);
					UnderLayer.Send(bytes,bytes.length,pLayerName);
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) { }
					
				}
				
					m_sHeader.capp_type=(byte)0x03;
					inputCut = new byte[talk.length - lengthCut];
					for(int i=0;i<inputCut.length;i++) {
						inputCut[i] = talk[i+lengthCut];
					}
					
					bytes = ObjToByte(m_sHeader,inputCut,inputCut.length);
					UnderLayer.Send(bytes,bytes.length,pLayerName);
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) { }
					
					wait=0;
				
			}else {
				
				m_sHeader.capp_type=(byte)0x00;
				byte[] bytes = ObjToByte(m_sHeader,input,length);
				
				UnderLayer.Send(bytes,bytes.length,pLayerName);
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) { }
				
				wait=0;
			}

		}
	}

	public byte[] ObjToByte(_CAPP_APP Header, byte[] input, int length) {
		byte[] buf = new byte[length + 4];
		m_sHeader.capp_data = new byte[length]; 
		buf[0] = Header.capp_totlen[0];
		buf[1] = Header.capp_totlen[1];
		buf[2] = Header.capp_type;
		buf[3] = Header.capp_unused;
		for (int i = 0; i < length; i++) {
			m_sHeader.capp_data[i] = input[i];
			buf[4 + i] = input[i];

		}
		return buf;
	}



	//①입력된채팅메시지의길이를측정
	//②입력된채팅메시지의길이가 1456bytes이하면,단편화없이전송
	//③입력된채팅메시지의길이가 1456bytes초과면,단편화 
	//④첫번째조각에채팅전체에대한정보를담아서전달  전체길이
	//⑤입력된채팅메시지를 1456bytes단위로단편화하여전송  이때 type을 통하여처음, 중간, 끝을구분해야 함	
	public boolean Send(byte[] input, int length) {
		
		thread = new Send_Thread(input,length, this.GetUnderLayer());
		Thread obj = new Thread(thread); //Thread 생성
		obj.start();//Thread 시작

		return false;

	}


	public byte[] RemoveCappHeader(byte[] input, int length) {

		m_sHeader.capp_data = new byte[length-4]; 
		byte[] rebuf = new byte[length-4];
		
		for (int i = 0; i < length-4; i++) {
			m_sHeader.capp_data[i] = input[4 + i];
			rebuf[i] = input[4 + i];
		}
		return rebuf;
	}


	public synchronized boolean Receive(byte[] input) {
		byte[] data;

		if(input[2] == (byte)0x00) {
			data = RemoveCappHeader(input, input.length);
			this.GetUpperLayer(0).Receive(data);

		}else {

			int length = (input[1] & 0xff)<<8 | (input[0] & 0xff);

			if(input[2] == (byte)0x01) {
				chatBuf = ByteBuffer.allocate(length);
				chatBuf.clear();

				data = RemoveCappHeader(input, input.length);
				for(int i=0;i<1456;i++) {
					chatBuf.put(data[i]);
				}
			}else if(input[2] == (byte)0x02) {
				data = RemoveCappHeader(input, input.length);
				for(int i=0;i<1456;i++) {
					chatBuf.put(data[i]);
				}
			}else if(input[2] == (byte)0x03) {
				data = RemoveCappHeader(input, input.length);
				int remain_legth = chatBuf.remaining();
				for(int i=0;i < remain_legth ;i++) {
					chatBuf.put(data[i]);
				}

				if(chatBuf.remaining() == 0) { //수정 : 채팅데이터 길이만큼 생성한 버퍼에 남은게 없을 경우 오류 없이 모두 입력된것이다. 상위 레이어로 보낸다. 
					this.GetUpperLayer(0).Receive(chatBuf.array());
				}
			}

		}



		return true;
	}



	@Override
	public String GetLayerName() {
		// TODO Auto-generated method stub
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		// TODO Auto-generated method stub
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
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		// TODO Auto-generated method stub
		if (pUnderLayer == null)
			return;
		this.p_UnderLayer = pUnderLayer;
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
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);
	}
}