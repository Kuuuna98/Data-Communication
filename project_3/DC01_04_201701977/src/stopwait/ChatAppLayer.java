package stopwait;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.swing.SingleSelectionModel;

public class ChatAppLayer implements BaseLayer {

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
	private byte[] talk = null;
	private int lengthCut=0;
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
	}

	public byte[] ObjToByte(_CAPP_APP Header, byte[] input, int length) {
		byte[] buf = new byte[length + 4];
		buf[0] = Header.capp_totlen[0];
		buf[1] = Header.capp_totlen[1];
		buf[2] = Header.capp_type;
		buf[3] = Header.capp_unused;
		for (int i = 0; i < length; i++) {
			buf[4 + i] = input[i];

		}
		return buf;
	}

	public void sleep(int time){

		try {

			Thread.sleep(time);

		} catch (InterruptedException e) { }

	}

	//①입력된채팅메시지의길이를측정
	//②입력된채팅메시지의길이가 10bytes이하면,단편화없이전송
	//③입력된채팅메시지의길이가 10bytes초과면,단편화 
	//④첫번째조각에채팅전체에대한정보를담아서전달  전체길이
	//⑤입력된채팅메시지를 10bytes단위로단편화하여전송  이때 type을 통하여처음, 중간, 끝을구분해야 함	
	public boolean Send(byte[] input, int length) {

		while(input.length == length && wait !=0 ) {
			sleep(1);
		}

		m_sHeader.capp_totlen[0] = (byte) (length %255);
		m_sHeader.capp_totlen[1] = (byte) (length /255);

		if(input.length != length && length == 0) {
			if(wait==3) wait=0;
			if(lengthCut == 0) {
				return false;
			}
		}

		if(length > 10 || (input.length != length && length == 0)) {

			byte[] inputCut;

			if(length > 10) {
				if(wait==2 || wait==3) return false;
				wait=1;
				talk = input;
				m_sHeader.capp_type=(byte)0x01;
				inputCut = new byte[10];
				lengthCut=0;
				for(int i=0;i<10;i++) {
					inputCut[i] = talk[i];
				}
				lengthCut = lengthCut+10;
				//	System.out.println("1: "+new String(inputCut));
				byte[] bytes = ObjToByte(m_sHeader,inputCut, inputCut.length);
				this.GetUnderLayer().Send(bytes,bytes.length);
			}
			else if(talk.length - lengthCut > 10) {
				if(wait==0 || wait==3) return false;
				wait=2;
				m_sHeader.capp_type=(byte)0x02;
				inputCut = new byte[10];
				for(int i=0;i<10;i++) {
					inputCut[i] = talk[i+lengthCut];
				}
				//System.out.println("2: "+new String(inputCut));
				lengthCut = lengthCut+10;
				byte[] bytes = ObjToByte(m_sHeader,inputCut,inputCut.length);
				this.GetUnderLayer().Send(bytes,bytes.length);
			}
			else if(talk.length - lengthCut <= 10) {
				if(wait==0 || wait==3) return false;
				m_sHeader.capp_type=(byte)0x03;
				inputCut = new byte[talk.length - lengthCut];
				for(int i=0;i<inputCut.length;i++) {
					inputCut[i] = talk[i+lengthCut];
				}
				//System.out.println("3: "+new String(inputCut));
				byte[] bytes = ObjToByte(m_sHeader,inputCut,inputCut.length);
				this.GetUnderLayer().Send(bytes,bytes.length);
				wait=3;
				lengthCut=0;
			}
		}else {
			if(wait==1 || wait==2 || wait==3) return false;
			//wait=0;
			//talk = input;
			lengthCut = 0;
			m_sHeader.capp_type=(byte)0x00;
			byte[] bytes = ObjToByte(m_sHeader,input,length);
			//System.out.println("0: "+new String(bytes));
			this.GetUnderLayer().Send(bytes,length+4);
			wait=3;
		}

		return true;
	}


	public byte[] RemoveCappHeader(byte[] input, int length) {

		byte[] rebuf = new byte[length-4];
		for (int i = 0; i < length-4; i++) {

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

			int length = (int)input[1]*255 +(int)input[0]; 
			///////15
			if(input[2] == (byte)0x01) {
				chatBuf = ByteBuffer.allocate(length);
				chatBuf.clear();

				data = RemoveCappHeader(input, input.length);
				for(int i=0;i<10;i++) {
					chatBuf.put(data[i]);
				}
			}else if(input[2] == (byte)0x02) {
				data = RemoveCappHeader(input, input.length);
				for(int i=0;i<10;i++) {
					chatBuf.put(data[i]);
				}
			}else if(input[2] == (byte)0x03) {
				data = RemoveCappHeader(input, input.length);
				int remain_legth = chatBuf.remaining();
				for(int i=0;i < remain_legth ;i++) {
					chatBuf.put(data[i]);
				}

				this.GetUpperLayer(0).Receive(chatBuf.array());

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