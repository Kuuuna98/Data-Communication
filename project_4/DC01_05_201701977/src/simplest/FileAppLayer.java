package simplest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;




public class FileAppLayer implements BaseLayer {


	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	private Send_Thread thread;
	private int nu = 0;

	private byte[] fileT = null;

	private int file_totlen;
	private int file_fill ;

	private int progress_number;
	private File file1;
	private FileOutputStream fos;

	private ByteBuffer fileBuf;

	public class  _FAPP_HEADER{

		byte[] fapp_totlen; //사용자가 입력한 문자열의 길이를 저장
		byte[] fapp_type; //단편화에 대한 정보를 담을 수 있다.
		//0x00 – 단편화되지않음, 0x01 – 단편화첫부분, 0x02 – 단편화중간, 0x03- 단 편화마지막		
		byte fapp_msg_type;
		byte ed;
		byte[] fapp_seq_num;
		byte[] fapp_data;

		public _FAPP_HEADER() {
			this.fapp_totlen = new byte[4];
			this.fapp_type = new byte[2];
			this.fapp_msg_type=0x00;
			this.ed = 0x00;
			this.fapp_seq_num=new byte[4];
			this.fapp_data = null;
		}
	}


	_FAPP_HEADER m_sHeader = new _FAPP_HEADER();

	public FileAppLayer(String pName) { 
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName; 

	}

	public byte[] ObjToByte(_FAPP_HEADER Header, byte[] input, int length) {
		byte[] buf = new byte[length + 12];
		buf[0] = Header.fapp_totlen[0];
		buf[1] = Header.fapp_totlen[1];
		buf[2] = Header.fapp_totlen[2];
		buf[3] = Header.fapp_totlen[3];
		buf[4] = Header.fapp_type[0];
		buf[5] = Header.fapp_type[1];
		buf[6] = Header.fapp_msg_type;
		buf[7] = Header.ed;
		buf[8] = Header.fapp_seq_num[0];
		buf[9] = Header.fapp_seq_num[1];
		buf[10] = Header.fapp_seq_num[2];
		buf[11] = Header.fapp_seq_num[3];

		for (int i = 0; i < length; i++) {
			buf[12 + i] = input[i];

		}
		return buf;
	}

	class Send_Thread implements Runnable{ //패킷수신을위한Runnable클래스
		//Pcap처리에필요한네트워크어뎁터및상위레이어객체초기화
		byte[] input;
		int length;
		BaseLayer UnderLayer;
		public Send_Thread(byte[] input,  int length, BaseLayer m_UnderLayer) {
			this.input = input;
			this.length = length;
			UnderLayer = m_UnderLayer;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
		

				SimplestDlg.File_search_Button.setEnabled(false);
				SimplestDlg.File_send_Button.setEnabled(false);
				SimplestDlg.FileNameArea.setText("파일 보내는 중");


				String filen = new String(input).trim();

				File file = new File(filen);
				FileInputStream fis = new FileInputStream(file);

				String[] filename = filen.split("\\\\");

				byte[] filenamebyte = filename[filename.length-1].trim().getBytes();

				int file_length = (int)file.length();
				byte[] databyte = new byte[file_length];
				fis.read(databyte,0,databyte.length);


				fis.close();

				
				fileT = databyte;
				m_sHeader.fapp_msg_type =(byte)0x00;

				int lengthCut=0;
				int seq_num = 0;

				m_sHeader.fapp_seq_num[3] =(byte) ((seq_num >> 24 ) & 0xff );
				m_sHeader.fapp_seq_num[2] =(byte)( (seq_num >> 16) & 0xff);
				m_sHeader.fapp_seq_num[1] =(byte) ((seq_num >> 8 ) & 0xff);
				m_sHeader.fapp_seq_num[0] =(byte) (seq_num & 0xff);

				m_sHeader.fapp_totlen[3] =(byte) ((file_length >> 24 ) & 0xff );
				m_sHeader.fapp_totlen[2] =(byte)( (file_length >> 16) & 0xff);
				m_sHeader.fapp_totlen[1] =(byte) ((file_length >> 8 ) & 0xff);
				m_sHeader.fapp_totlen[0] =(byte) (file_length & 0xff);


				byte[] bytes = ObjToByte(m_sHeader,filenamebyte, filenamebyte.length);

				UnderLayer.Send(bytes,bytes.length,pLayerName);

				try {
					Thread.sleep(1);
				} catch (InterruptedException e) { }

				SimplestDlg.progress.setMaximum(file_length);
				SimplestDlg.progress.setMinimum(0);
				progress_number = 0;
				PrograssB();

				m_sHeader.fapp_msg_type=(byte)0x01;

				byte[] inputCut;
				
				file_length = 1448;
				m_sHeader.fapp_totlen[3] =(byte) ((file_length >> 24 ) & 0xff );
				m_sHeader.fapp_totlen[2] =(byte)( (file_length >> 16) & 0xff);
				m_sHeader.fapp_totlen[1] =(byte) ((file_length >> 8 ) & 0xff);
				m_sHeader.fapp_totlen[0] =(byte) (file_length & 0xff);

				while(fileT.length - lengthCut > 1448) {


					m_sHeader.fapp_seq_num[3] =(byte) ((seq_num >> 24 ) & 0xff );
					m_sHeader.fapp_seq_num[2] =(byte)( (seq_num >> 16) & 0xff);
					m_sHeader.fapp_seq_num[1] =(byte) ((seq_num >> 8 ) & 0xff);
					m_sHeader.fapp_seq_num[0] =(byte) (seq_num & 0xff);

					m_sHeader.fapp_type[0] = (byte)0x00;
					m_sHeader.fapp_type[1] = (byte)0x01;

					inputCut = new byte[1448];

					for(int i=0;i<1448;i++) {
						inputCut[i] = fileT[i+lengthCut];
					}


					lengthCut = lengthCut+1448;
					bytes = ObjToByte(m_sHeader,inputCut,inputCut.length);
					UnderLayer.Send(bytes,bytes.length,pLayerName);

					progress_number = lengthCut;
					seq_num++;

					try {
						Thread.sleep(1);
					} catch (InterruptedException e) { }

				}


				m_sHeader.fapp_seq_num[3] =(byte) ((seq_num >> 24 ) & 0xff );
				m_sHeader.fapp_seq_num[2] =(byte) ((seq_num >> 16) & 0xff);
				m_sHeader.fapp_seq_num[1] =(byte) ((seq_num >> 8 ) & 0xff);
				m_sHeader.fapp_seq_num[0] =(byte) (seq_num & 0xff);


				m_sHeader.fapp_type[0] = (byte)0x00;
				m_sHeader.fapp_type[1] = (byte)0x02;
				
				file_length = fileT.length - lengthCut;
				
				m_sHeader.fapp_totlen[3] =(byte) ((file_length >> 24 ) & 0xff );
				m_sHeader.fapp_totlen[2] =(byte)( (file_length >> 16) & 0xff);
				m_sHeader.fapp_totlen[1] =(byte) ((file_length >> 8 ) & 0xff);
				m_sHeader.fapp_totlen[0] =(byte) (file_length & 0xff);
				
				inputCut = new byte[fileT.length - lengthCut];
				
				for(int i=0;i<(fileT.length - lengthCut) ;i++) {
					inputCut[i] = fileT[i+lengthCut];
				}
				
				bytes = ObjToByte(m_sHeader,inputCut,inputCut.length);

				
				
				UnderLayer.Send(bytes,bytes.length,pLayerName);
				progress_number = fileT.length;
			
				try {
					Thread.sleep(2);
				} catch (InterruptedException e) { }
			
				lengthCut=0;
				progress_number = 0;

				SimplestDlg.FileNameArea.setText("");
				SimplestDlg.File_search_Button.setEnabled(true);


			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (NullPointerException e) {
				// TODO Auto-generated catch block
				SimplestDlg.FileNameArea.setText("");
				SimplestDlg.File_search_Button.setEnabled(true);
				e.printStackTrace();
			}

		}

	}

	//①선택된파일의길이를측정 
	//②선택된파일의길이가1448bytes이하면,단편화없이전송 u fapp_msg_type사용해서파일정보,데이터구분해서전송 
	//③선택된파일의길이가1448bytes초과면,단편화 
	//④첫번째조각에파일에대한필요정보를담아서전달 u 전체길이,파일명(확장자)등 
	//⑤선택된파일의1448bytes단위로단편화하여전송 u 1448bytes만큼파일에서읽어와서구조체의data에저장 u fapp_seq_num에 단편화된 패킷의 순서를 저장 u 이때fapp_type을통하여처음, 중간, 끝을구분해야함 
	//⑥모든단편화된조각을보내면서progress bar를수정 u (보낸조각개수/ 단편화된조각총개수)등을이용
	
	public boolean Send(byte[] input,  int length)  {

		thread = new Send_Thread(input,length, this.GetUnderLayer());
		Thread obj = new Thread(thread); //Thread 생성
		obj.start();//Thread 시작

		return false;

	}


	public byte[] RemoveCappHeader(byte[] input, int length) {

		byte[] rebuf = new byte[length-12];
		for (int i = 0; i < length-12; i++) {

			rebuf[i] = input[12 + i];
		}
		return rebuf;
	}


	//①전달받은첫조각을통해단편화여부를확인 u 전체길이를통해1448bytes 초과라면단편화가되어있을것임
	//②단편화가되어있지않다면, fapp_msg_type확인해서패킷을두번받은후파일저 장 u Ex)msg_type= 0x00이면파일명,msg_type= 0x01이면데이터) 
	//③단편화가되어있다면, buffer를활용하여파일이모두전달될때까지덧붙임 u 대부분의파일은1448bytes를상당히초과할것임 u List 등의자료구조를활용하여단편화된조각의순서를고려하여저장해야함 
	//§ 코드에조각순서고려부분이없으면감점.(ex. 정렬사용,index와seq_num비교해서저장) 
	//④모든조각을전달받았다면, buffer에쓰여진내용을파일로저장 u fapp_type을통하여모든조각을전달받았는지확인할수있음 
	//⑤단편화된조각을받으면서progress bar를수정 u (받은조각개수/ 단편화된조각총개수)등을이용public boolean Send(byte[] input, int length) {


	public synchronized boolean Receive(byte[] input) {


		SimplestDlg.File_search_Button.setEnabled(false);
		SimplestDlg.File_send_Button.setEnabled(false);
		SimplestDlg.FileNameArea.setText("파일 받는 중");

		byte[] data;


		int length = (input[3] & 0xff)<<24 | (input[2] & 0xff)<<16 | (input[1] & 0xff)<<8 | (input[0] & 0xff);
		int sqn_num =  (input[11] & 0xff)<<24 | (input[10] & 0xff)<<16 | (input[9] & 0xff)<<8 | (input[8] & 0xff);

		

		if(input[6] == (byte)0x00) {
			file_totlen = length;
			data = RemoveCappHeader(input, input.length);

			this.fileBuf = ByteBuffer.allocate(length);
			this.fileBuf.clear();

			try {
				
				String path = new String(data).trim();
				file1 = new File(path); 
				file1.createNewFile();

				fos = new FileOutputStream(file1);

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			SimplestDlg.progress.setMaximum(file_totlen);
			SimplestDlg.progress.setMinimum(0);
			file_fill = 0;
			progress_number = 0;
			PrograssB();


		}else if(input[6] == (byte)0x01) {

			if(input[4]== (byte)00 && input[5] == (byte)01) {
				data = RemoveCappHeader(input, input.length);

				for(int i=0;i<1448;i++) {
					//코드에조각순서고려
				//	System.out.println( "//"+length+"[[["+ fileBuf.remaining() +" : "+ ((int)(sqn_num * 1448) + (int)i));
					this.fileBuf.put(((sqn_num * 1448) + i), data[i]);
				}

				file_fill = file_fill+1448;
				
			}else if(input[4]== (byte)00 && input[5] == (byte)02) {
				
				data = RemoveCappHeader(input, input.length);

				
				for(int i=0;i < length ;i++) {
//					//코드에조각순서고려
					this.fileBuf.put((sqn_num * 1448) + i, data[i]);
				}
				
				file_fill = file_fill+ length;	
			} 

		}

		progress_number = (file_fill) ;



		if(file_totlen == (file_fill)) {

			byte[] success = this.fileBuf.array();

			try {
				fos.write(success, 0, success.length);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			

			progress_number = 0 ;
			file_totlen = 0;
			SimplestDlg.File_search_Button.setEnabled(true);
			SimplestDlg.FileNameArea.setText("");

			try {
				if(fos != null) fos.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}

		return true;

	}

	public void PrograssB() {
		new Thread() {
			public void run() {
				while(true) {
					SimplestDlg.progress.setValue(progress_number);  
				}
			}
		}.start();

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
