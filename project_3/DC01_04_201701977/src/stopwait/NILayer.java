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
	//thread ����
	//NILayer-> (Receive�Լ�) : ���ŵ� Ethernet Frame�� �޾Ƽ� ���� ���̾�� �����ϴ� ������ ��


	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();



	//��������
	int m_iNumAdater; //��Ʈ��ũ ��� �ε��� ([27]�� ����)
	public Pcap m_AdapterObject; //��Ʈ��ũ ��� ��ü
	public PcapIf device; //��Ʈ��ũ �������̽� ��ü
	public List<PcapIf> m_pAdapterList; // ��Ʈ��ũ �������̽� ���
	StringBuilder errbuf = new StringBuilder(); //���� ����

	public NILayer(String pName) { //������
		// super(pName);
		pLayerName = pName; 

		m_pAdapterList = new ArrayList<PcapIf>(); //[27]������ ���� �Ҵ�
		m_iNumAdater = 0; //[24]������ 0���� �ʱ�ȭ
		SetAdapterList(); // ��Ʈ��ũ ��� ��� �������� �Լ� ȣ��

	}
	


	public void PacketStartDriver() {//��Ŷ ����̹� �����Լ�
		//Pcap ���ۿ� �ʿ��� �⺻ ������ ���� ������
		int snaplen = 64*1024; //��Ŷ ĸó ����
		int flags = Pcap.MODE_PROMISCUOUS; //��Ŷ ĸó �÷��� (PROMISCUOUS; ��� ��Ŷ)
		int timeout = 10*1000; //��Ŷ ĸó �ð� (���� �ð� ���� ��Ŷ�� ���ŵ��� ���� ��� �������ۿ� �Է�)
		m_AdapterObject = Pcap.openLive(m_pAdapterList.get(m_iNumAdater).getName(), snaplen, flags, timeout, errbuf);
		//���õ� ��Ʈ��ũ ��� �� ������ �ɼǿ� ������ pcap �۵� ����
	}



	public void setAdapterNumber(int iNum) { //��Ʈ��ũ ��� �����Լ�
		m_iNumAdater = iNum; //���õ� ��Ʈ��ũ ��� �ε����� ���� �ʱ�ȭ
		PacketStartDriver();//��Ŷ ����̹� �����Լ�(��Ʈ��ũ ��� ��ü open)
		Receive();//��Ŷ �����Լ�
	}

	public void SetAdapterList() { //��Ʈ��ũ ��� ��� ���� �Լ�

		int r = Pcap.findAllDevs(m_pAdapterList, errbuf); //���� ��ǻ�Ϳ� �����ϴ� ��� ��Ʈ��ũ ��� ��� ��������
		if(r == Pcap.NOT_OK || m_pAdapterList.isEmpty()) { //��Ʈ��ũ ��Ͱ� �ϳ��� �������� ���� ��� ���� ó��
			System.err.printf("Can't read list of devices, error is %s", errbuf, toString());
			return;
		}
	}

	public boolean Send(byte[] input, int length) { //��Ŷ ���� �Լ�
		ByteBuffer buf = ByteBuffer.wrap(input); //�������̾�κ��� ���޹��� �����͸� ����Ʈ ���ۿ� ����
		
		if(m_AdapterObject.sendPacket(buf) != Pcap.OK) { //��Ʈ��ũ ����� sendPacket()�Լ��� ���� ������ ����
			System.err.println(m_AdapterObject.getErr()); //��Ŷ ������ ������ ��� �����޽��� ��� �� false ��ȯ
			return false; //��Ŷ ������ ������ ��� true ��ȯ
		}
	

		return true;
	}

	public boolean Receive() {//��Ŷ ���� �Լ�
		//��Ŷ ���� �� ��Ŷ ó���� ���� runnable Ŭ���� ����
		Receive_Thread thread = new Receive_Thread(m_AdapterObject, this.GetUpperLayer(0));
		Thread obj = new Thread(thread); //Thread ����
		obj.start();//Thread ����

		return false;

	}
	

	class Receive_Thread implements Runnable{ //��Ŷ����������RunnableŬ����
		//Pcapó�����ʿ��ѳ�Ʈ��ũ��͹׻������̾ü�ʱ�ȭ
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
				//��Ŷ���������Ѷ��̺귯���Լ�(PcapPacketHandler)
				PcapPacketHandler<String> jpacketHandler = new PcapPacketHandler<String>() {
					public void nextPacket(PcapPacket packet, String user) {
						data = packet.getByteArray(0, packet.size()); //���ŵ���Ŷ�ǵ�����(����Ʈ�迭)�� ��Ŷũ�⸦�˾Ƴ� 
						UpperLayer.Receive(data); //���ŵȵ����͸��������̾������
					}
				};
				//��Ʈ��ũ��Ϳ���PcapPacketHandler�����ѹݺ�
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
