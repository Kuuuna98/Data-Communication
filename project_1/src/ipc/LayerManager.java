package ipc;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class LayerManager {
	
	private class _NODE{ //내부 클래스 노드
		private String token;
		private _NODE next;
		public _NODE(String input){ 
			this.token = input;
			this.next = null;
		}
	}

	_NODE mp_sListHead; //리스트의 헤더
	_NODE mp_sListTail; //리스트의 테일
	
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
	
	public void AddLayer(BaseLayer pLayer){ //layer를 추가함
		mp_aLayers.add(m_nLayerCount++, pLayer); //mp_aLayers배열의 m_nLayerCount번째에 pLayer를 넣고 m_nLayerCount를 하나 증가시킨다.
		//m_nLayerCount++;
	}
	
	
	public BaseLayer GetLayer(int nindex){
		return mp_aLayers.get(nindex); //mp_aLayers배열의 nindex번째 원소(layer)를 가져온다.
	}
	
	public BaseLayer GetLayer(String pName){
		for( int i=0; i < m_nLayerCount; i++){
			if(pName.compareTo(mp_aLayers.get(i).GetLayerName()) == 0) //만약 pName이 mp_aLayers배열 i번째 layer이름과 같을경우
				return mp_aLayers.get(i); //mp_aLayers배열 i번째 layer를 리턴한다.
		}
		return null; //아니라면 null을 리턴한다.
	}
	
	public void ConnectLayers(String pcList){
		MakeList(pcList);//String형 pcList 쪼개어 노드 list로 만든다.
		LinkLayer(mp_sListHead); //위 소스코드로 만들어진 노드 list의 처음을 가리키는 mp_sListHead를 이용하여 
	}

	private void MakeList(String pcList){
		StringTokenizer tokens = new StringTokenizer(pcList, " "); //StringTokenizer를 이용하여 pcList를 " "를 기준으로 나눈다.
		
		for(; tokens.hasMoreElements();){ //커서 다음으로 아직 남은 요소들이 있다면
			_NODE pNode = AllocNode(tokens.nextToken()); //tokens.nextToken()을 하여 다음으로 커서를 옮기고 그값을 노드로 만들고 pNode에 저장한다.
			AddNode(pNode); //노드 list에 pNode가 연결되도록 추가한다.
			
		}	
	}

	private _NODE AllocNode(String pcName){
		_NODE node = new _NODE(pcName); //pcName를  데이터값으로 하는  노드 node를 새롭게 생성한다.
				
		return node;	//node를 리턴한다.			
	}
	
	private void AddNode(_NODE pNode){
		if(mp_sListHead == null){ //mp_sListHead가 null일경우 만들어진 list 노드가 없다면
			mp_sListHead = mp_sListTail = pNode; //mp_sListHead과 mp_sListTail 둘 모두 pNode를 가리키도록 연결한다.
		}else{//아니라면
			mp_sListTail.next = pNode; //(mp_sListTail가 가리키고있는)마지막으로 연결된 노드의 next로 pNode를 연결한다.
			mp_sListTail = pNode; //mp_sListTail가 마지막 노드가 된 pNode를 가리키돌고 연결한다.
		}
	}

	private void Push (BaseLayer pLayer){ //스텍의 top에 pLayer를 쌓는다.
		mp_Stack.add(++m_nTop, pLayer); //mp_Stack에 m_nTop를 하나증가시킨값 번째에 pLayer를 넣는다. 
		//mp_Stack.add(pLayer);
		//m_nTop++;
	}

	private BaseLayer Pop(){//스텍의 top을 제거한다
		BaseLayer pLayer = mp_Stack.get(m_nTop); //mp_Stack의 m_nTop번째 원소를 얻어 pLayer에 저장한다.
		mp_Stack.remove(m_nTop); //mp_Stack배열에서 m_nTop번째 원소를 제거한다.
		m_nTop--; //m_nTop 값을 하나 줄인다.
		
		return pLayer; //제거한 pLayer를 반환한다.
	}
	
	private BaseLayer Top(){//스텍의 top 값을 반환한다.
		return mp_Stack.get(m_nTop); //mp_Stack의 m_nTop번째 원소를 반환한다.
	}
	
	private void LinkLayer(_NODE pNode){
		BaseLayer pLayer = null; 
		
		while(pNode != null){//pNode가 null일 때까지 while문을 반복한다.(pNode가 list의 마지막까지 참조할때까지)
			if( pLayer == null) //만약 pLayer가 null일경우 이는 pNode가 list의 처음 노드라는 것을 의미한다.
				pLayer = GetLayer (pNode.token); //GetLayer메소드를 호출하여 pNode.token과 이름이 같은 Layer를 반환하여 pLayer에 저장한다.
			else{ //pLayer가 null이 아닐경우
				if(pNode.token.equals("(")) //만약 pNode.token이 "("일경우
					Push (pLayer); //pLayer를 push하여 스텍에 쌓는다.
				else if(pNode.token.equals(")")) //만약 pNode.token이 ")"일경우
					Pop(); //pop하여 스텍에있는 layer를 뺀다.
				else{//괄호가 아니라면
					char cMode = pNode.token.charAt(0); //pNode.token의 0번째 char형 문자를 cMode에 저장한다.(기호가 저장될것이다.)
					String pcName = pNode.token.substring(1, pNode.token.length()); //pNode.token의 1번째부터 pNode.token.length()번째까지(0번째 문자를 뺀 전체)의 substring을 psName에 저장한다.
					
					pLayer = GetLayer (pcName); //pLayer는 pcName와 이름이 동일한 layer를 가져와 저장한다.
					
					switch(cMode){ //swich문을 이용하여 cMode값에 따라
					case '*': //cMode가 *이라면
						Top().SetUpperUnderLayer( pLayer ); //Top()에서 반환받은 layer와 pLayer가 양방향 연결되도록 한다.
						break;
					case '+'://cMode가 +라면
						Top().SetUpperLayer( pLayer );//Top()에서 반환받은 layer의 윗방향으로 pLayer가 연결되도록 한다.
						break;
					case '-'://cMode가 -라면
						Top().SetUnderLayer( pLayer );//Top()에서 반환받은 layer의 아래방향으로 pLayer가 연결되도록 한다.
						break;
					}					
				}
			}
			
			pNode = pNode.next; //pNode가 현재 가리키고 있는 노드의 다음 노드를 참조하도록 한다.
				
		}
	}
	
	
}
