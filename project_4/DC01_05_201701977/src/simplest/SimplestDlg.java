package simplest;

import java.awt.Color;
import java.awt.Container;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JProgressBar;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class SimplestDlg extends JFrame implements BaseLayer  	{

	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	BaseLayer UnderLayer;
	public int progress_number;

	private static LayerManager m_LayerMgr = new LayerManager();

	private JTextField ChattingWrite;
	//파일전송 /*ch_test*/
	static JTextField FileNameArea;

	Container contentPane;

	JTextArea ChattingArea;
	JTextArea srcAddress;
	JTextArea dstAddress;
	JComboBox strCombo;

	JLabel choice;
	JLabel lblsrc;
	JLabel lbldst;

	JButton Setting_Button;
	JButton Chat_send_Button;

	static JButton File_search_Button;	
	static JButton File_send_Button;

	FileDialog fdOpen;

	static JProgressBar progress;
	static JComboBox<String> NICComboBox;

	int adapterNumber = 0;
	int index;
	String Text;
	String FileNameText;

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		m_LayerMgr.AddLayer(new NILayer("NIL"));
		m_LayerMgr.AddLayer(new EthernetLayer("Ethernet"));
		m_LayerMgr.AddLayer(new ChatAppLayer("Chat"));
		m_LayerMgr.AddLayer(new FileAppLayer("File"));
		m_LayerMgr.AddLayer(new SimplestDlg("GUI"));

		m_LayerMgr.ConnectLayers(" NIL ( *Ethernet ( *Chat ( *GUI ) *File ( *GUI ) ) ) ");
	}


	public SimplestDlg(String pName) throws IOException {
		pLayerName = pName;

		setTitle("simplest");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(250, 250, 644, 425);
		contentPane = new JPanel();
		((JComponent) contentPane).setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel chattingPanel = new JPanel();// chatting panel
		chattingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "chatting",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		chattingPanel.setBounds(10, 5, 360, 276);
		contentPane.add(chattingPanel);
		chattingPanel.setLayout(null);

		JPanel chattingEditorPanel = new JPanel();// chatting write panel
		chattingEditorPanel.setBounds(10, 15, 340, 210);
		chattingPanel.add(chattingEditorPanel);
		chattingEditorPanel.setLayout(null);

		ChattingArea = new JTextArea();
		ChattingArea.setEditable(false);
		ChattingArea.setBounds(0, 0, 340, 210);
		chattingEditorPanel.add(ChattingArea);// chatting edit

		JPanel chattingInputPanel = new JPanel();// chatting write panel
		chattingInputPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		chattingInputPanel.setBounds(10, 230, 250, 20);
		chattingPanel.add(chattingInputPanel);
		chattingInputPanel.setLayout(null);

		ChattingWrite = new JTextField();
		ChattingWrite.setBounds(2, 2, 250, 20);// 249
		chattingInputPanel.add(ChattingWrite);
		ChattingWrite.setColumns(10);// writing area

		/*ch_test*/
		//FileNameArea
		JPanel FileNamePanel = new JPanel();// FileName panel
		FileNamePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "파일전송",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		FileNamePanel.setBounds(10, 280, 360, 90);
		contentPane.add(FileNamePanel);
		FileNamePanel.setLayout(null);


		JPanel FileNameInputPanel = new JPanel();
		FileNameInputPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		FileNameInputPanel.setBounds(10, 25, 250, 20);		
		FileNamePanel.add(FileNameInputPanel);
		FileNameInputPanel.setLayout(null);

		FileNameArea = new JTextField();
		FileNameArea.setBounds(2, 2, 250, 20);	
		FileNameArea.setEditable(false);
		FileNameInputPanel.add(FileNameArea);
		FileNameArea.setColumns(10);// writing area


		/*progress bar*/
		progress = new JProgressBar();
		progress.setValue(0);
		progress.setBounds(10, 55, 250, 20);
		FileNamePanel.add(progress);
		progress.setStringPainted(true);
		

		JPanel settingPanel = new JPanel();
		settingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "setting",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		settingPanel.setBounds(380, 5, 236, 371);
		contentPane.add(settingPanel);
		settingPanel.setLayout(null);

		JPanel sourceAddressPanel = new JPanel();
		sourceAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		sourceAddressPanel.setBounds(10, 96, 170, 20);
		settingPanel.add(sourceAddressPanel);
		sourceAddressPanel.setLayout(null);


		lblsrc = new JLabel("Source Address");
		lblsrc.setBounds(10, 75, 170, 20);
		settingPanel.add(lblsrc);


		srcAddress = new JTextArea();
		srcAddress.setBounds(2, 2, 170, 20);
		srcAddress.setEditable(false);
		sourceAddressPanel.add(srcAddress);// src address

		JPanel destinationAddressPanel = new JPanel();
		destinationAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		destinationAddressPanel.setBounds(10, 212, 170, 20);
		settingPanel.add(destinationAddressPanel);
		destinationAddressPanel.setLayout(null);


		////////////////////
		choice = new JLabel("NIC 선택");
		choice.setBounds(10, 30, 170, 20);
		settingPanel.add(choice);

		String[] adapterna= new String[((NILayer) m_LayerMgr.GetLayer("NIL")).m_pAdapterList.size()];


		for(int i=0;i<((NILayer) m_LayerMgr.GetLayer("NIL")).m_pAdapterList.size();i++)
			adapterna[i] = ((NILayer) m_LayerMgr.GetLayer("NIL")).m_pAdapterList.get(i).getDescription();

		strCombo= new JComboBox(adapterna);
		strCombo.setBounds(10, 50, 190, 20);
		strCombo.setVisible(true);
		settingPanel.add(strCombo);
		strCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource(); // 콤보박스 알아내기
				index = cb.getSelectedIndex();// 선택된 아이템의 인덱스

				try {
					byte[] mac = ((NILayer) m_LayerMgr.GetLayer("NIL")).m_pAdapterList.get(index).getHardwareAddress();

					final StringBuilder buf = new StringBuilder();
					for(byte b:mac) {
						if(buf.length()!=0) buf.append("-");
						if(b>=0 && b<16) buf.append('0');
						buf.append(Integer.toHexString((b<0)? b+256:b).toUpperCase());
					}
					srcAddress.setText(buf.toString());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});


		lbldst = new JLabel("Destination Address");
		lbldst.setBounds(10, 187, 190, 20);
		settingPanel.add(lbldst);

		dstAddress = new JTextArea();
		dstAddress.setBounds(2, 2, 170, 20);
		destinationAddressPanel.add(dstAddress);// dst address

		Setting_Button = new JButton("Setting");// setting
		Setting_Button.setBounds(80, 270, 100, 20);
		Setting_Button.addActionListener(new setAddressListener());
		settingPanel.add(Setting_Button);// setting

		Chat_send_Button = new JButton("Send");
		Chat_send_Button.setBounds(270, 230, 80, 20);
		Chat_send_Button.addActionListener(new setAddressListener());
		chattingPanel.add(Chat_send_Button);// chatting send button

		File_search_Button = new JButton("file...");
		File_search_Button.setBounds(270, 25, 80, 20);
		File_search_Button.addActionListener(new setAddressListener());
		FileNamePanel.add(File_search_Button);

		fdOpen = new FileDialog(this, "파일열기", FileDialog.LOAD);


		File_send_Button = new JButton("Send");
		File_send_Button.setBounds(270, 55, 80, 20);
		File_send_Button.addActionListener(new setAddressListener());
		FileNamePanel.add(File_send_Button);
		File_send_Button.setEnabled(false);





		setVisible(true);

	}




	class setAddressListener implements ActionListener  {
		@Override
		public void actionPerformed(ActionEvent e) {


			if (e.getSource() == Setting_Button) {
				if(Setting_Button.getText() == "Setting") {
					String srcAd = srcAddress.getText();
					String dstAd = dstAddress.getText();

					if(srcAd==null || dstAd==null ) {
						ChattingArea.setText("주소설정오류: 주소 setting이 안됐습니다.\n");
					}else {
						String[] valuesS = srcAd.split("-");
						byte[] src = new byte[6];
						for(int i=0;i<valuesS.length;i++) {
							src[i]=(byte) Integer.parseInt(valuesS[i],16);
						}


						String[] valuesD = dstAd.split("-");

						byte[] dst = new byte[6];
						for(int i=0;i<6;i++) {
							dst[i] = (byte) Integer.parseInt(valuesD[i],16);
						}




						((EthernetLayer) m_LayerMgr.GetLayer("Ethernet")).SetEnetSrcAddress(src);
						((EthernetLayer) m_LayerMgr.GetLayer("Ethernet")).SetEnetDstAddress(dst);

						((NILayer) m_LayerMgr.GetLayer("NIL")).setAdapterNumber(index);


						ChattingArea.setText("");
						Setting_Button.setText("Reset");

						//SrcAddress, DstAddress의값변경못하게설정
						dstAddress.setEnabled(false);
						srcAddress.setEnabled(false);
						strCombo.setEnabled(false);

					}

				}else { //Setting_Button.getText() == "Reset"
					//“Reset” Button을누를시SrcAddress, DstAddress의text를공백으로변경 

					srcAddress.setText("");
					dstAddress.setEnabled(true);
					srcAddress.setEnabled(true);
					strCombo.setEnabled(true);
					dstAddress.setText("");
					ChattingArea.setText("");


					Setting_Button.setText("Setting");
				}
			}

			if(e.getSource() == Chat_send_Button) {
				if(Setting_Button.getText() == "Reset") {

					Text = ChattingWrite.getText();
					ChattingWrite.setText("");

					String ex = ChattingArea.getText();
					ChattingArea.setText(ex+"[SEND]: "+Text+"\n");

					byte[] sbchange = Text.getBytes();
					((ChatAppLayer) m_LayerMgr.GetLayer("Chat")).Send(sbchange,Text.getBytes().length);


				}else {
					//주소값이없으면“주소설정오류” MessageDialog를띄운다.
					ChattingArea.setText("주소설정오류: 주소 setting이 안됐습니다.\n");
				}
			}
			if(e.getSource() == File_search_Button) {

				fdOpen.setVisible(true);

				String name = fdOpen.getFile();
				String path = fdOpen.getDirectory() + name;
				FileNameText = path;

				if(name != null) {				
					FileNameArea.setText(name);
				}else {
					JOptionPane.showMessageDialog(null, "파일을 선택하지않았습니다.",
							"경고", JOptionPane.WARNING_MESSAGE);

					FileNameArea.setText("");
					File_send_Button.setEnabled(false);

					return;
				}
				File_send_Button.setEnabled(true);

			}
			if(e.getSource() == File_send_Button) {
				if(Setting_Button.getText() == "Reset") {

					FileNameArea.setText("");
					byte[] fichange = FileNameText.getBytes();
					
					((FileAppLayer) m_LayerMgr.GetLayer("File")).Send(fichange,fichange.length);
					File_send_Button.setEnabled(false);
					
					progress_number = 0;

				}else {
					//주소값이없으면“주소설정오류” MessageDialog를띄운다.
					ChattingArea.setText("주소설정오류: 주소 setting이 안됐습니다.\n");
				}

			}

		}
	}


	public boolean Receive(byte[] input) {	

		String ex = ChattingArea.getText();
		String ne = new String(input);

		ChattingArea.setText(ex+"[RECV]: "+ne+"\n");

		return true;
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
