package ipc;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class LayerManager {
	
	private class _NODE{ //���� Ŭ���� ���
		private String token;
		private _NODE next;
		public _NODE(String input){ 
			this.token = input;
			this.next = null;
		}
	}

	_NODE mp_sListHead; //����Ʈ�� ���
	_NODE mp_sListTail; //����Ʈ�� ����
	
	private int m_nTop;
	private int m_nLayerCount;

	private ArrayList<BaseLayer> mp_Stack = new ArrayList<BaseLayer>(); 
	private ArrayList<BaseLayer> mp_aLayers = new ArrayList<BaseLayer>() ;
	

	public LayerManager(){
		m_nLayerCount = 0;
		mp_sListHead = null;
		mp_sListTail = null;
		m_nTop = -1;
	}
	
	public void AddLayer(BaseLayer pLayer){ //layer�� �߰���
		mp_aLayers.add(m_nLayerCount++, pLayer); //mp_aLayers�迭�� m_nLayerCount��°�� pLayer�� �ְ� m_nLayerCount�� �ϳ� ������Ų��.
		//m_nLayerCount++;
	}
	
	
	public BaseLayer GetLayer(int nindex){
		return mp_aLayers.get(nindex); //mp_aLayers�迭�� nindex��° ����(layer)�� �����´�.
	}
	
	public BaseLayer GetLayer(String pName){
		for( int i=0; i < m_nLayerCount; i++){
			if(pName.compareTo(mp_aLayers.get(i).GetLayerName()) == 0) //���� pName�� mp_aLayers�迭 i��° layer�̸��� �������
				return mp_aLayers.get(i); //mp_aLayers�迭 i��° layer�� �����Ѵ�.
		}
		return null; //�ƴ϶�� null�� �����Ѵ�.
	}
	
	public void ConnectLayers(String pcList){
		MakeList(pcList);//String�� pcList �ɰ��� ��� list�� �����.
		LinkLayer(mp_sListHead); //�� �ҽ��ڵ�� ������� ��� list�� ó���� ����Ű�� mp_sListHead�� �̿��Ͽ� 
	}

	private void MakeList(String pcList){
		StringTokenizer tokens = new StringTokenizer(pcList, " "); //StringTokenizer�� �̿��Ͽ� pcList�� " "�� �������� ������.
		
		for(; tokens.hasMoreElements();){ //Ŀ�� �������� ���� ���� ��ҵ��� �ִٸ�
			_NODE pNode = AllocNode(tokens.nextToken()); //tokens.nextToken()�� �Ͽ� �������� Ŀ���� �ű�� �װ��� ���� ����� pNode�� �����Ѵ�.
			AddNode(pNode); //��� list�� pNode�� ����ǵ��� �߰��Ѵ�.
			
		}	
	}

	private _NODE AllocNode(String pcName){
		_NODE node = new _NODE(pcName); //pcName��  �����Ͱ����� �ϴ�  ��� node�� ���Ӱ� �����Ѵ�.
				
		return node;	//node�� �����Ѵ�.			
	}
	
	private void AddNode(_NODE pNode){
		if(mp_sListHead == null){ //mp_sListHead�� null�ϰ�� ������� list ��尡 ���ٸ�
			mp_sListHead = mp_sListTail = pNode; //mp_sListHead�� mp_sListTail �� ��� pNode�� ����Ű���� �����Ѵ�.
		}else{//�ƴ϶��
			mp_sListTail.next = pNode; //(mp_sListTail�� ����Ű���ִ�)���������� ����� ����� next�� pNode�� �����Ѵ�.
			mp_sListTail = pNode; //mp_sListTail�� ������ ��尡 �� pNode�� ����Ű���� �����Ѵ�.
		}
	}

	private void Push (BaseLayer pLayer){ //������ top�� pLayer�� �״´�.
		mp_Stack.add(++m_nTop, pLayer); //mp_Stack�� m_nTop�� �ϳ�������Ų�� ��°�� pLayer�� �ִ´�. 
		//mp_Stack.add(pLayer);
		//m_nTop++;
	}

	private BaseLayer Pop(){//������ top�� �����Ѵ�
		BaseLayer pLayer = mp_Stack.get(m_nTop); //mp_Stack�� m_nTop��° ���Ҹ� ��� pLayer�� �����Ѵ�.
		mp_Stack.remove(m_nTop); //mp_Stack�迭���� m_nTop��° ���Ҹ� �����Ѵ�.
		m_nTop--; //m_nTop ���� �ϳ� ���δ�.
		
		return pLayer; //������ pLayer�� ��ȯ�Ѵ�.
	}
	
	private BaseLayer Top(){//������ top ���� ��ȯ�Ѵ�.
		return mp_Stack.get(m_nTop); //mp_Stack�� m_nTop��° ���Ҹ� ��ȯ�Ѵ�.
	}
	
	private void LinkLayer(_NODE pNode){
		BaseLayer pLayer = null; 
		
		while(pNode != null){//pNode�� null�� ������ while���� �ݺ��Ѵ�.(pNode�� list�� ���������� �����Ҷ�����)
			if( pLayer == null) //���� pLayer�� null�ϰ�� �̴� pNode�� list�� ó�� ����� ���� �ǹ��Ѵ�.
				pLayer = GetLayer (pNode.token); //GetLayer�޼ҵ带 ȣ���Ͽ� pNode.token�� �̸��� ���� Layer�� ��ȯ�Ͽ� pLayer�� �����Ѵ�.
			else{ //pLayer�� null�� �ƴҰ��
				if(pNode.token.equals("(")) //���� pNode.token�� "("�ϰ��
					Push (pLayer); //pLayer�� push�Ͽ� ���ؿ� �״´�.
				else if(pNode.token.equals(")")) //���� pNode.token�� ")"�ϰ��
					Pop(); //pop�Ͽ� ���ؿ��ִ� layer�� ����.
				else{//��ȣ�� �ƴ϶��
					char cMode = pNode.token.charAt(0); //pNode.token�� 0��° char�� ���ڸ� cMode�� �����Ѵ�.(��ȣ�� ����ɰ��̴�.)
					String pcName = pNode.token.substring(1, pNode.token.length()); //pNode.token�� 1��°���� pNode.token.length()��°����(0��° ���ڸ� �� ��ü)�� substring�� psName�� �����Ѵ�.
					
					pLayer = GetLayer (pcName); //pLayer�� pcName�� �̸��� ������ layer�� ������ �����Ѵ�.
					
					switch(cMode){ //swich���� �̿��Ͽ� cMode���� ����
					case '*': //cMode�� *�̶��
						Top().SetUpperUnderLayer( pLayer ); //Top()���� ��ȯ���� layer�� pLayer�� ����� ����ǵ��� �Ѵ�.
						break;
					case '+'://cMode�� +���
						Top().SetUpperLayer( pLayer );//Top()���� ��ȯ���� layer�� ���������� pLayer�� ����ǵ��� �Ѵ�.
						break;
					case '-'://cMode�� -���
						Top().SetUnderLayer( pLayer );//Top()���� ��ȯ���� layer�� �Ʒ��������� pLayer�� ����ǵ��� �Ѵ�.
						break;
					}					
				}
			}
			
			pNode = pNode.next; //pNode�� ���� ����Ű�� �ִ� ����� ���� ��带 �����ϵ��� �Ѵ�.
				
		}
	}
	
	
}
