package stopwait;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;


public class NILayer implements BaseLayer  {
	//thread 구현
	//NILayer-> (Receive함수) : 수신된 Ethernet Frame을 받아서 상위 레이어로 전달하는 역할을 담


	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();



	//변수선언
	int m_iNumAdater; //네트워크 어뎁터 인덱스 ([27]에 사용됨)
	public Pcap m_AdapterObject; //네트워크 어뎁터 객체
	public PcapIf device; //네트워크 인터페이스 객체
	public List<PcapIf> m_pAdapterList; // 네트워크 인터페이스 목록
	StringBuilder errbuf = new StringBuilder(); //에러 버퍼

	public NILayer(String pName) { //생성자
		// super(pName);
		pLayerName = pName; 

		m_pAdapterList = new ArrayList<PcapIf>(); //[27]변수를 동적 할당
		m_iNumAdater = 0; //[24]변수를 0으로 초기화
		SetAdapterList(); // 네트워크 어뎁터 목록 가져오기 함수 호출

	}
	


	public void PacketStartDriver() {//패킷 드라이버 시작함수
		//Pcap 동작에 필요한 기본 설정을 위한 변수들
		int snaplen = 64*1024; //패킷 캡처 길이
		int flags = Pcap.MODE_PROMISCUOUS; //패킷 캡처 플래그 (PROMISCUOUS; 모든 패킷)
		int timeout = 10*1000; //패킷 캡처 시간 (설정 시간 동안 패킷이 수신되지 않은 경우 에러버퍼에 입력)
		m_AdapterObject = Pcap.openLive(m_pAdapterList.get(m_iNumAdater).getName(), snaplen, flags, timeout, errbuf);
		//선택된 네트워크 어뎁터 및 설정된 옵션에 맞춰진 pcap 작동 시작
	}



	public void setAdapterNumber(int iNum) { //네트워크 어뎁터 설정함수
		m_iNumAdater = iNum; //선택된 네트워크 어뎁터 인덱스로 변수 초기화
		PacketStartDriver();//패킷 드라이버 시작함수(네트워크 어뎁터 객체 open)
		Receive();//패킷 수신함수
	}

	public void SetAdapterList() { //네트워크 어뎁터 목록 생성 함수

		int r = Pcap.findAllDevs(m_pAdapterList, errbuf); //현재 컴퓨터에 존재하는 모든 네트워크 어뎁터 목록 가져오기
		if(r == Pcap.NOT_OK || m_pAdapterList.isEmpty()) { //네트워크 어뎁터가 하나도 존재하지 않을 경우 에러 처리
			System.err.printf("Can't read list of devices, error is %s", errbuf, toString());
			return;
		}
	}

	public boolean Send(byte[] input, int length) { //패킷 전송 함수
		ByteBuffer buf = ByteBuffer.wrap(input); //상위레이어로부터 전달받은 데이터를 바이트 버퍼에 담음
		
		if(m_AdapterObject.sendPacket(buf) != Pcap.OK) { //네트워크 어뎁터의 sendPacket()함수를 통해 데이터 전송
			System.err.println(m_AdapterObject.getErr()); //패킷 전송이 실패한 경우 에러메시지 출력 및 false 반환
			return false; //패킷 전송이 성공한 경우 true 반환
		}
	

		return true;
	}

	public boolean Receive() {//패킷 수신 함수
		//패킷 수신 시 패킷 처리를 위한 runnable 클래스 생성
		Receive_Thread thread = new Receive_Thread(m_AdapterObject, this.GetUpperLayer(0));
		Thread obj = new Thread(thread); //Thread 생성
		obj.start();//Thread 시작

		return false;

	}
	

	class Receive_Thread implements Runnable{ //패킷수신을위한Runnable클래스
		//Pcap처리에필요한네트워크어뎁터및상위레이어객체초기화
		byte[] data;
		Pcap AdapterObject;
		BaseLayer UpperLayer;
		public Receive_Thread(Pcap m_AdapterObject, BaseLayer m_UpperLayer) {
			AdapterObject = m_AdapterObject;
			UpperLayer = m_UpperLayer;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(true) {
				//패킷수신을위한라이브러리함수(PcapPacketHandler)
				PcapPacketHandler<String> jpacketHandler = new PcapPacketHandler<String>() {
					public void nextPacket(PcapPacket packet, String user) {
						data = packet.getByteArray(0, packet.size()); //수신된패킷의데이터(바이트배열)와 패킷크기를알아냄 
						UpperLayer.Receive(data); //수신된데이터를상위레이어로전달
					}
				};
				//네트워크어뎁터에서PcapPacketHandler를무한반복
				AdapterObject.loop(100000, jpacketHandler, "");
			}
		}

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
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);

	}
	

}
