package com.tetris.window;

import java.awt.Color;import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.ImageIcon; //추가
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JOptionPane; //추가
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.tetris.classes.Block;
import com.tetris.classes.TetrisBlock;
import com.tetris.controller.TetrisController;
import com.tetris.main.Music; //0525
import com.tetris.main.TetrisMain;
import com.tetris.network.GameClient;
import com.tetris.shape.CenterUp;
import com.tetris.shape.LeftTwoUp;
import com.tetris.shape.LeftUp;
import com.tetris.shape.Line;
import com.tetris.shape.Nemo;
import com.tetris.shape.RightTwoUp;
import com.tetris.shape.RightUp;

public class TetrisBoard extends JPanel implements Runnable, KeyListener, MouseListener, ActionListener{
	private static final long serialVersionUID = 1L;
	
	private Tetris tetris;
	private GameClient client;

	public static final int BLOCK_SIZE = 20;
	public static final int BOARD_X = 140;
	public static final int BOARD_Y = 50;
	private int minX=1, maxX=10, maxY=21, down=50, up=0;
	private final int MESSAGE_WIDTH = BLOCK_SIZE * 7;
	private final int MESSAGE_HEIGHT = BLOCK_SIZE * 6;
	private final int PANEL_WIDTH = 2*(maxX*BLOCK_SIZE + MESSAGE_WIDTH + BOARD_X);
	private final int PANEL_HEIGHT = maxY*BLOCK_SIZE + MESSAGE_HEIGHT + BOARD_Y;
	
	private SystemMessageArea systemMsg = new SystemMessageArea(BLOCK_SIZE*1,BOARD_Y + BLOCK_SIZE + BLOCK_SIZE*7, BLOCK_SIZE*5, BLOCK_SIZE*12);
	private MessageArea messageArea = new MessageArea(this,0, PANEL_HEIGHT - MESSAGE_HEIGHT, PANEL_WIDTH-BLOCK_SIZE*7, MESSAGE_HEIGHT);
	private JButton btnStart = new JButton("시작하기");
	private JButton btnExit = new JButton("나가기");
	private JCheckBox checkGhost = new JCheckBox("고스트모드",true);
	private JCheckBox checkGrid  = new JCheckBox("격자 표시",true);
	private JCheckBox checkEffect =new JCheckBox("효과음",true);
	private JCheckBox checkBGM= new JCheckBox("배경음악",true);
	private Integer[] lv = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
	private JComboBox<Integer> comboSpeed = new JComboBox<Integer>(lv);
	
	private String ip;
	private int port;
	private String nickName;
	private Thread th;
	private ArrayList<Block> blockList;
	private ArrayList<TetrisBlock> nextBlocks;
	private TetrisBlock shap;
	private TetrisBlock ghost;
	private TetrisBlock hold;
	private Block[][] map;
	private TetrisController controller;
	private TetrisController controllerGhost;
	
	private TetrisBlock shap2;//HK
	private TetrisBlock hold2;//HK
	private ArrayList<Block> blockList2;//HK
	private ArrayList<TetrisBlock> nextBlocks2;//HK
	
	private boolean isPlay = false;
	private boolean isHold = false;
	private boolean usingGhost = true;
	private boolean usingGrid = true;
	private int removeLineCount = 0;
	private int removeLineCombo = 0;
	
	public static boolean usingEffect= true;
	public static boolean usingBGM=true; //라인 추가 05/22
	public Music GameMusic ;
	public Music GameEndSound;
	public Sound Sound;// 추가 
	public ImageIcon icon; // 추가
	public ImageIcon icon2; // 추가
	
	public TetrisBoard(Tetris tetris, GameClient client) {
		this.tetris = tetris;
		this.client = client;
		this.setPreferredSize(new Dimension(PANEL_WIDTH,PANEL_HEIGHT));//기본크기
		this.addKeyListener(this);
		this.addMouseListener(this);
		this.setLayout(null);
		this.setFocusable(true);
		
		btnStart.setBounds(PANEL_WIDTH - BLOCK_SIZE*7, PANEL_HEIGHT - messageArea.getHeight(), BLOCK_SIZE*7, messageArea.getHeight()/2);
		btnStart.setFocusable(false);
		btnStart.setEnabled(false);
		btnStart.addActionListener(this);
		
		btnStart.addMouseListener(new MouseAdapter() {
			/*public void mousePressed(MouseEvent e) {
				Music MousePressedSound =new Music("Swoosh.mp3", false);
				MousePressedSound.start();
			}*/
			public void mouseEntered(MouseEvent e) {
				Music MousePressedSound= new Music("Magic_Chime.mp3",false);
				MousePressedSound.start();
			}
		
		});//0525 효과음(버튼)추가
		btnExit.addMouseListener(new MouseAdapter() {
			/*public void mousePressed(MouseEvent e) {
				Music MousePressedSound =new Music("Front_Door_Close.mp3", false);
				MousePressedSound.start();
			}*/
			public void mouseEntered(MouseEvent e) {
				Music MousePressedSound= new Music("Magic_Chime.mp3",false);
				MousePressedSound.start();
			}
		
		});//0525 효과음(버튼)추가
		
		btnExit.setBounds(PANEL_WIDTH - BLOCK_SIZE*7, PANEL_HEIGHT - messageArea.getHeight()/2, BLOCK_SIZE*7, messageArea.getHeight()/2);
		btnExit.setFocusable(false);	
		btnExit.addActionListener(this);
		checkGhost.setBounds(PANEL_WIDTH - BLOCK_SIZE*12+35,5,95,20);
		checkGhost.setBackground(new Color(29,64,184));
		checkGhost.setForeground(Color.WHITE);
		checkGhost.setFont(new Font("굴림", Font.BOLD,13));
		checkGhost.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				usingGhost = checkGhost.isSelected();
				TetrisBoard.this.setRequestFocusEnabled(true);
				TetrisBoard.this.repaint();
			}
		});
		checkGrid.setBounds(PANEL_WIDTH - BLOCK_SIZE*12+35,25,95,20);
		checkGrid.setBackground(new Color(29,64,184));
		checkGrid.setForeground(Color.WHITE);
		checkGrid.setFont(new Font("굴림", Font.BOLD,13));
		checkGrid.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				usingGrid = checkGrid.isSelected();
				TetrisBoard.this.setRequestFocusEnabled(true);
				TetrisBoard.this.repaint();
			}
		});
		checkEffect.setBounds(PANEL_WIDTH - BLOCK_SIZE * 7 + 35, 5, 95, 20);		//효과음 checkbox 위치 및 디자인
		checkEffect.setBackground(new Color(29,64,184));
		checkEffect.setForeground(Color.white);
		checkEffect.setFont(new Font("굴림", Font.BOLD, 13));
		checkEffect.setRequestFocusEnabled(false);
		checkEffect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (checkEffect.isSelected() == true) {
						usingEffect = true;
				} else {
						usingEffect = false;
				}		
				TetrisBoard.this.setRequestFocusEnabled(true);
				TetrisBoard.this.repaint();
			}
		}); //Effect sound checkbox
             
		checkBGM.setBounds(PANEL_WIDTH - BLOCK_SIZE * 7 + 35, 25, 95, 20);			//배경음악 checkbox 위치 및 디자인
		checkBGM.setBackground(new Color(29,64,184));
		checkBGM.setForeground(Color.white);
		checkBGM.setFont(new Font("굴림", Font.BOLD, 13));
		checkBGM.setRequestFocusEnabled(false);
		checkBGM.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (checkBGM.isSelected() == true) {

					usingBGM = true;
					if(GameMusic.isAlive() || GameMusic != null) {
						GameMusic.close();
						GameMusic = new Music("Tetris.mp3", true );
						GameMusic.start();
					}else {
						GameMusic = new Music("Tetris.mp3", true );
						GameMusic.start();
					}
				} else {
					usingBGM = false;
					if(GameMusic != null || GameMusic.isAlive()) {
						GameMusic.close();
					}
					
				}	
				TetrisBoard.this.setRequestFocusEnabled(true);
				TetrisBoard.this.repaint();
			}
		});//BGM checkbox

		comboSpeed.setBounds(PANEL_WIDTH - BLOCK_SIZE*13, 5, 45, 20);
		this.add(comboSpeed);
		
		this.add(systemMsg);
		this.add(messageArea);
		this.add(btnStart);		
		this.add(btnExit);
		this.add(checkGhost);
		this.add(checkGrid);
		
		this.add(checkEffect);// 효과음 추가
		this.add(checkBGM);// 배경화면 추가
		icon = new ImageIcon(TetrisMain.class.getResource("../../../Images/desert-1654439_1920.jpg")); //배경으로 쓸 사진 파일 불러오기
		
		// 배경화면 사이즈 조절
		/*
		 * Image temp = icon.getImage(); Image temp2 = temp.getScaledInstance(1000, 600,
		 * Image.SCALE_SMOOTH); icon2 = new ImageIcon(temp2);
		 */
	}
	
	public void startNetworking(String ip, int port, String nickName){
		this.ip = ip;
		this.port = port;
		this.nickName = nickName;
		this.repaint();
	}
	
	/**TODO : 게임시작
	 * 게임을 시작한다.
	 */
	
	
	public void gameStart(int speed){
		comboSpeed.setSelectedItem(new Integer(speed));
		//돌고 있을 스레드를 정지시킨다.
		if(th!=null){
			try {isPlay = false;th.join();} 
			catch (InterruptedException e) {e.printStackTrace();}
		}
		
		//맵셋팅
		map = new Block[maxY][maxX];
		blockList = new ArrayList<Block>();
		nextBlocks = new ArrayList<TetrisBlock>();
		blockList2 = new ArrayList<Block>();//HK
		nextBlocks2 = new ArrayList<TetrisBlock>();//HK
		
		//도형셋팅
		shap = getRandomTetrisBlock();
		ghost = getBlockClone(shap,true);
		hold = null;
		isHold = false;
		controller = new TetrisController(shap,maxX,maxY,map);
		controllerGhost = new TetrisController(ghost,maxX,maxY,map);
		this.showGhost();
		for(int i=0 ; i<5 ; i++){
			nextBlocks.add(getRandomTetrisBlock());
		}
		
		//스레드 셋팅
		isPlay = true;
		th = new Thread(this);
		th.start();
	}
	
	
	//TODO : paint
	@Override
	protected void paintComponent(Graphics g) {
		
		// 전체의 배경이 되는 하얀 캔버스
		g.clearRect(0, 0, this.getWidth(), this.getHeight());
		g.setColor(Color.WHITE);
		/*
		// ip, port, nickname이 표시되는 보드 (스크롤, 체크박스는 포함하지 않음)
		g.setColor(new Color(164,104,67));
		g.fillRect(0, 0, 2*(2*BOARD_X+maxX*BLOCK_SIZE), BOARD_Y);
		// Hold, Next가 표시되는 부분이 있는 좌우 네모박스
		g.setColor(new Color(189,142,98));
		g.fillRect(0, BOARD_Y, 2*(2*BOARD_X+maxX*BLOCK_SIZE), maxY*BLOCK_SIZE);
		*/
		g.drawImage(icon.getImage(),0, 0, null); // 배경화면 그리기
		// g.drawImage(icon2.getImage(),0, 0, null); // 사이즈 조절된 배경화면 그리기
		
		// 보드 내 폰트 설정
		Font font= g.getFont();
		g.setFont(new Font(font.getFontName(), font.getStyle(), font.getSize()));
		g.setColor(Color.WHITE);
		// IP 출력
		g.drawString("ip : "+ip+"    port : "+port, 20, 20);
		//NickName 출력
		g.drawString("닉네임 : "+nickName, 20, 40);
		// 속도 출력
		g.setFont(new Font("굴림", Font.BOLD,13));
		g.drawString("속도", PANEL_WIDTH - BLOCK_SIZE*10, 20);
		g.setFont(font);
			
		// <<1p 화면>>
		// 까만 배경 부분
		g.setColor(Color.BLACK);
		// 가운데
		g.fillRect(BOARD_X, BOARD_Y, maxX*BLOCK_SIZE, maxY*BLOCK_SIZE);
		// 왼쪽
		g.fillRect(BLOCK_SIZE*minX ,BOARD_Y + BLOCK_SIZE, BLOCK_SIZE*5,BLOCK_SIZE*5);
		// 오른쪽 위
		g.fillRect(BOARD_X + maxX*BLOCK_SIZE+ BLOCK_SIZE*minX, BOARD_Y + BLOCK_SIZE, BLOCK_SIZE*5,BLOCK_SIZE*5);
		// 오른쪽 아래
		g.fillRect(BOARD_X + maxX*BLOCK_SIZE + BLOCK_SIZE*minX, BOARD_Y + BLOCK_SIZE + BLOCK_SIZE*7, BLOCK_SIZE*5,BLOCK_SIZE*12);
			
		//HOLD  NEXT 출력
		g.setFont(new Font(font.getFontName(),font.getStyle(),20));
		g.setColor(Color.BLACK);
		g.drawString("H O L D", BLOCK_SIZE*minX + 15, BOARD_Y + BLOCK_SIZE + BLOCK_SIZE*5 + 20);
		g.drawString("N E X T", BOARD_X + maxX*BLOCK_SIZE+ BLOCK_SIZE*minX + 15, BOARD_Y + BLOCK_SIZE + BLOCK_SIZE*5 + 20);
		g.setFont(font);
				
		//그리드 표시
		if(usingGrid){
			g.setColor(Color.darkGray);
			// 가운데 가로줄
			for(int i=1;i<maxY;i++) 
				g.drawLine(BOARD_X, BOARD_Y+BLOCK_SIZE*i, BOARD_X + maxX*BLOCK_SIZE, BOARD_Y + BLOCK_SIZE*i);
			// 가운데 세로줄
			for(int i=1;i<maxX;i++) 
				g.drawLine(BOARD_X + BLOCK_SIZE*i, BOARD_Y, BOARD_X + BLOCK_SIZE*i, BOARD_Y + maxY*BLOCK_SIZE);
			// 왼쪽 가로줄
			for(int i=1;i<5;i++) 
				g.drawLine(BLOCK_SIZE*minX ,BOARD_Y + BLOCK_SIZE*(i+minX), BLOCK_SIZE*(minX+5),BOARD_Y + BLOCK_SIZE*(i+minX));
			// 왼쪽 세로줄
			for(int i=1;i<5;i++) 
				g.drawLine(BLOCK_SIZE*(i+minX) ,BOARD_Y + BLOCK_SIZE*minX, BLOCK_SIZE*(i+minX),BOARD_Y + BLOCK_SIZE*(minX+5));
			// 오른쪽 위 가로줄
			for(int i=1;i<5;i++) 
				g.drawLine(BOARD_X + BLOCK_SIZE*minX + maxX*BLOCK_SIZE, BOARD_Y + BLOCK_SIZE*(i+minX), 
				BOARD_X + maxX*BLOCK_SIZE + BLOCK_SIZE*(minX+5), BOARD_Y + BLOCK_SIZE*(i+minX));
			// 오른쪽 위 세로줄
			for(int i=1;i<5;i++) 
				g.drawLine(BOARD_X + maxX*BLOCK_SIZE + BLOCK_SIZE*(i+minX), BOARD_Y + BLOCK_SIZE*minX, 
				BOARD_X + maxX*BLOCK_SIZE + BLOCK_SIZE*(i+minX), BOARD_Y + BLOCK_SIZE*(minX+5));	
		}
		// 1p 2p 사이선
		g.drawLine(this.getWidth()/2, BOARD_Y, this.getWidth()/2, BOARD_Y+maxY*BLOCK_SIZE);
			
		// <<2p 화면>>
		// 까만 배경 부분
		g.setColor(Color.BLACK);
		// 가운데
		g.fillRect(3*BOARD_X+maxX*BLOCK_SIZE, BOARD_Y, maxX*BLOCK_SIZE, maxY*BLOCK_SIZE);
		// 왼쪽
		g.fillRect(2*BOARD_X+maxX*BLOCK_SIZE+BLOCK_SIZE*minX,BOARD_Y + BLOCK_SIZE, BLOCK_SIZE*5,BLOCK_SIZE*5);
		// 오른쪽 위
		g.fillRect(3*BOARD_X + 2*maxX*BLOCK_SIZE+BLOCK_SIZE*minX, BOARD_Y + BLOCK_SIZE, BLOCK_SIZE*5,BLOCK_SIZE*5);
		// 오른쪽 아래
		g.fillRect(3*BOARD_X + 2*maxX*BLOCK_SIZE+BLOCK_SIZE*minX, BOARD_Y + BLOCK_SIZE + BLOCK_SIZE*7, BLOCK_SIZE*5,BLOCK_SIZE*12);
		
		//HOLD  NEXT 출력
		g.setFont(new Font(font.getFontName(),font.getStyle(),20));
		g.setColor(Color.BLACK);
		g.drawString("H O L D", 2*BOARD_X+maxX*BLOCK_SIZE+BLOCK_SIZE*minX + 15, BOARD_Y + BLOCK_SIZE + BLOCK_SIZE*5 + 20);
		g.drawString("N E X T", 3*BOARD_X + 2*maxX*BLOCK_SIZE+ BLOCK_SIZE*minX + 15, BOARD_Y + BLOCK_SIZE + BLOCK_SIZE*5 + 20);
		g.setFont(font);
		
		//그리드 표시
		if(usingGrid){
			g.setColor(Color.darkGray);
			// 가운데 가로줄
			for(int i=1;i<maxY;i++) 
				g.drawLine(3*BOARD_X+maxX*BLOCK_SIZE, BOARD_Y+BLOCK_SIZE*i, 3*BOARD_X + 2*maxX*BLOCK_SIZE, BOARD_Y + BLOCK_SIZE*i);
			// 가운데 세로줄
			for(int i=1;i<maxX;i++) 
				g.drawLine(3*BOARD_X+maxX*BLOCK_SIZE + BLOCK_SIZE*i, BOARD_Y, 3*BOARD_X+maxX*BLOCK_SIZE + BLOCK_SIZE*i, BOARD_Y + maxY*BLOCK_SIZE);
			// 왼쪽 가로줄
			for(int i=1;i<5;i++) 
				g.drawLine(2*BOARD_X+maxX*BLOCK_SIZE+BLOCK_SIZE*minX, BOARD_Y + BLOCK_SIZE*(i+minX), 2*BOARD_X+maxX*BLOCK_SIZE+BLOCK_SIZE*(minX+5),BOARD_Y + BLOCK_SIZE*(i+minX));
			// 왼쪽 세로줄
			for(int i=1;i<5;i++) 
				g.drawLine(2*BOARD_X+maxX*BLOCK_SIZE+BLOCK_SIZE*(i+minX),BOARD_Y + BLOCK_SIZE*minX, 2*BOARD_X+maxX*BLOCK_SIZE+BLOCK_SIZE*(i+minX),BOARD_Y + BLOCK_SIZE*(minX+5));
			// 오른쪽 위 가로줄
			for(int i=1;i<5;i++) 
				g.drawLine(3*BOARD_X + 2*maxX*BLOCK_SIZE+BLOCK_SIZE*minX, BOARD_Y + BLOCK_SIZE*(i+minX), 
						3*BOARD_X + 2*maxX*BLOCK_SIZE+BLOCK_SIZE*(minX+5), BOARD_Y + BLOCK_SIZE*(i+minX));
			// 오른쪽 위 세로줄
			for(int i=1;i<5;i++) 
				g.drawLine(3*BOARD_X + 2*maxX*BLOCK_SIZE+BLOCK_SIZE*(i+minX), BOARD_Y + BLOCK_SIZE*minX, 
						3*BOARD_X + 2*maxX*BLOCK_SIZE+BLOCK_SIZE*(i+minX), BOARD_Y + BLOCK_SIZE*(minX+5));	
		}
		// 홀드 블록 그리기
		int x=0,y=0,newY=0;
		if(hold!=null){
			x=0; y=0; newY=3;
			x = hold.getPosX();
			y = hold.getPosY();
			hold.setPosX(-4);
			hold.setPosY(newY);
			hold.drawBlock(g);
			hold.setPosX(x);
			hold.setPosY(y);
		}
		// 다음 블록 그리기
		if(nextBlocks!=null){
			x=0; y=0; newY=3;
			for(int i = 0 ; i<nextBlocks.size() ; i++){
				TetrisBlock block = nextBlocks.get(i);
				x = block.getPosX();
				y = block.getPosY();
				block.setPosX(13);
				block.setPosY(newY);
				if(newY==3) newY=6;
				block.drawBlock(g);
				block.setPosX(x);
				block.setPosY(y);
				newY+=3;
			}
		}
		//쌓이는 블록 그리기
		if(blockList!=null){
			x=0; y=0;
			for(int i = 0 ; i<blockList.size() ; i++){
				Block block = blockList.get(i);
				x = block.getPosGridX();
				y = block.getPosGridY();
				block.setPosGridX(x);
				block.setPosGridY(y);
				block.drawColorBlock(g);
			}
		}
		// 고스트 블록 그리기
		if(ghost!=null){

			if(usingGhost){
				x=0; y=0;
				x = ghost.getPosX();
				y = ghost.getPosY();
				ghost.setPosX(x);
				ghost.setPosY(y);
				ghost.drawBlock(g);
			}
		}
		//떨어지는 블록 그리기
		if(shap!=null){
			x=0; y=0;
			x = shap.getPosX();
			y = shap.getPosY();
			shap.setPosX(x);
			shap.setPosY(y);
			shap.drawBlock(g);
		}
		//상대블록을 그리는 것을 유지한다.
		try {
			drawBlockShap(shap2, g);
			drawBlockDeposit(blockList2, g);
			drawBlockHold(hold2, g);
			drawBlockNext(nextBlocks2, g);
		}catch(NullPointerException e) {
		e.printStackTrace();
		}//repaint_drawBlock
	}

	/* 상대 블록 그리기 HK */
	public void drawBlockShap(TetrisBlock shap, Graphics g) {
		if(shap!=null){

			int x=0, y=0;
			x = shap.getPosX();
			y = shap.getPosY();
			shap.setPosX(x+24);
			shap.setPosY(y);
			shap.drawBlock(g);
			shap.setPosX(x);
			shap.setPosY(y);
		}
	}
	public void drawBlockShap(TetrisBlock shap) {
		drawBlockShap(shap, getGraphics());
	}//drawBlockShap

	public void drawBlockDeposit(ArrayList<Block> blockList, Graphics g) {
		if(blockList!=null){
			
			int x=0, y=0;
			for(int i = 0 ; i<blockList.size() ; i++){
				Block block = blockList.get(i);
				x = block.getPosGridX();
				y = block.getPosGridY();
				block.setPosGridX(x+24);
				block.setPosGridY(y);
				block.drawColorBlock(g);
				block.setPosGridX(x);
				block.setPosGridY(y);
			}
		}
	}
	public void drawBlockDeposit(ArrayList<Block> blockList) {
		drawBlockDeposit(blockList, getGraphics());
	}//drawBlockDeposit
	
	public void drawBlockHold(TetrisBlock hold, Graphics g) {
		if(hold!=null){
			
			int x=0, y=0, newY=3;
			x = hold.getPosX();
			y = hold.getPosY();
			hold.setPosX(20);
			hold.setPosY(newY);
			hold.drawBlock(g);
			hold.setPosX(x);
			hold.setPosY(y);
		}
	}
	public void drawBlockHold(TetrisBlock hold) {
		drawBlockHold(hold, getGraphics());
	}//drawBlockHold
	
	public void drawBlockNext(ArrayList<TetrisBlock> nextBlocks, Graphics g) {
		if(nextBlocks!=null){
			
			int x=0, y=0, newY=3;
			for(int i = 0 ; i<nextBlocks.size() ; i++){
				TetrisBlock block = nextBlocks.get(i);
				x = block.getPosX();
				y = block.getPosY();
				block.setPosX(37);
				block.setPosY(newY);
				if(newY==3) newY=6;
				block.drawBlock(g);
				block.setPosX(x);
				block.setPosY(y);
				newY+=3;
			}
		}
	}
	public void drawBlockNext(ArrayList<TetrisBlock> nextBlocks) {
		drawBlockNext(nextBlocks, getGraphics());
	}//drawBlockNext
	
	@Override
	public void run() {
		int countMove = (21-(int)comboSpeed.getSelectedItem())*5;
		int countDown = 0;
		int countUp = up;
		
		while(isPlay){
			synchronized (TetrisBoard.class) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(countDown!=0){
				countDown--;
				if(countDown==0){
					
					if(controller!=null && !controller.moveDown()) this.fixingTetrisBlock();
				}
				this.repaint();
				continue;
			}
			
			countMove--;
			if (countMove == 0) {
				countMove = (21-(int)comboSpeed.getSelectedItem())*5;
				if (controller != null && !controller.moveDown()) countDown = down;
				else this.showGhost();
			}
			
			if (countUp != 0) {
				countUp--;
				if (countUp == 0) {
					countUp = up;
					addBlockLine(1);
				}
			}
			//controller.showIndex();
			this.repaint();
			}//synchronized() HK
		}//while()
	}//run()

	
	/**
	 * 맵(보이기, 논리)을 상하로 이동한다.
	 * @param lineNumber	
	 * @param num -1 or 1
	 */
	public void dropBoard(int lineNumber, int num){
		
		// 맵을 떨어트린다.
		this.dropMap(lineNumber,num);
		
		//좌표바꿔주기(1만큼증가)
		this.changeTetrisBlockLine(lineNumber,num);
		
		//다시 체크하기
		this.checkMap();
		
		//고스트 다시 뿌리기
		this.showGhost();
	}
	
	
	/**
	 * lineNumber의 위쪽 라인들을 모두 num칸씩 내린다.
	 * @param lineNumber
	 * @param num 칸수 -1,1
	 */
	private void dropMap(int lineNumber, int num) {
		if(num==1){
			//한줄씩 내리기
			for(int i= lineNumber ; i>0 ;i--){
				for(int j=0 ; j<map[i].length ;j++){
					map[i][j] = map[i-1][j];
				}
			}
			
			//맨 윗줄은 null로 만들기
			for(int j=0 ; j<map[0].length ;j++){
				map[0][j] = null;
			}
		}
		else if(num==-1){
			//한줄씩 올리기
			for(int i= 1 ; i<=lineNumber ;i++){
				for(int j=0 ; j<map[i].length ;j++){
					map[i-1][j] = map[i][j];
				}
			}
			
			//removeLine은 null로 만들기
			for(int j=0 ; j<map[0].length ;j++){
				map[lineNumber][j] = null;
			}
		}
	}
	
	
	/**
	 * lineNumber의 위쪽 라인들을 모두 num만큼 이동시킨다.
	 * @param lineNumber 
	 * @param num	이동할 라인
	 */
	private void changeTetrisBlockLine(int lineNumber, int num){
		int y=0, posY=0;
		for(int i=0 ; i<blockList.size() ; i++){
			y = blockList.get(i).getY();
			posY = blockList.get(i).getPosGridY();
			if(y<=lineNumber)blockList.get(i).setPosGridY(posY + num);
		}
	}

	
	/**
	 * 테트리스 블럭을 고정시킨다. 
	 */
	private void fixingTetrisBlock() {
		synchronized (this) {
			if(stop){
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		boolean isCombo = false;
		removeLineCount = 0;
		
		// drawList 추가
		for (Block block : shap.getBlock()) {
			blockList.add(block);
		}
		
		// check
		isCombo = checkMap();

		if(isCombo) removeLineCombo++;
		else removeLineCombo = 0;
		
		//콜백메소드
		this.getFixBlockCallBack(blockList,removeLineCombo,removeLineCount);
		
		//다음 테트리스 블럭을 가져온다.
		this.nextTetrisBlock();
		
		//홀드가능상태로 만들어준다.
		isHold = false;
	}//fixingTetrisBlock()
	
	
	/**
	 * 
	 * @return true-지우기성공, false-지우기실패
	 */
	private boolean checkMap(){
		boolean isCombo = false;
		int count = 0;
		Block mainBlock;
		
		for(int i=0 ; i<blockList.size() ;i++){
			mainBlock = blockList.get(i);
			
			// map에 추가
			if(mainBlock.getY()<0 || mainBlock.getY() >=maxY) continue;
			
			if(mainBlock.getY()<maxY && mainBlock.getX()<maxX) 
				map[mainBlock.getY()][mainBlock.getX()] = mainBlock;

			// 줄이 꽉 찼을 경우. 게임을 종료한다.
			if (mainBlock.getY() == 1 && mainBlock.getX() > 2 && mainBlock.getX() < 7) {
				this.gameEndCallBack();
				break;
			}
			
			//1줄개수 체크
			count = 0;
			for (int j = 0; j < maxX; j++) {
				if(map[mainBlock.getY()][j] != null) count++;
				
			}
			
			//block의 해당 line을 지운다.
			if (count == maxX) {
				removeLineCount++;
				this.removeBlockLine(mainBlock.getY());
				isCombo = true;
			}
		}
		return isCombo;
	}
	
	/**
	 * 테트리스 블럭 리스트에서 테트리스 블럭을 받아온다.
	 */
	public void nextTetrisBlock(){
		shap = nextBlocks.get(0);
		this.initController();
		nextBlocks.remove(0);
		nextBlocks.add(getRandomTetrisBlock());
	}
	private void initController(){
		controller.setBlock(shap);
		ghost = getBlockClone(shap,true);
		controllerGhost.setBlock(ghost);
	}
	
	/**
	 * lineNumber 라인을 삭제하고, drawlist에서 제거하고, map을 아래로 내린다.
	 * @param lineNumber 삭제라인
	 */
	private void removeBlockLine(int lineNumber) {
		if(usingEffect) new Music("Remove_Line.mp3",false).start(); // 라인 제거시 효과음 ! 06/10일
		
		// 1줄을 지워줌
		for (int j = 0; j < maxX ; j++) {
			for (int s = 0; s < blockList.size(); s++) {
				Block b = blockList.get(s);
				if (b == map[lineNumber][j])
					blockList.remove(s);
			}
			map[lineNumber][j] = null;
		}// for(j)

		this.dropBoard(lineNumber,1);
	}
	
	
	/**TODO : 게임종료콜벡
	 * 게임이 종료되면 실행되는 메소드
	 */
	public void gameEndCallBack(){
		client.gameover();
		this.isPlay = false;
		if(GameMusic != null && GameMusic.isAlive()) {
			GameMusic.close();
		}
		GameEndSound = new Music("GameOver.mp3", false); 
		GameEndSound.start();	
		//ImageIcon popupicon = new ImageIcon(TetrisMain.class.getResource("../../../Images/gameover.png"));
	}
	public void win() {
		ImageIcon popupicon = new ImageIcon(TetrisMain.class.getResource("../../../Images/giphy(2).gif"));
		JOptionPane.showMessageDialog(null, null, "Winer Winer Chicken Diner", JOptionPane.ERROR_MESSAGE, popupicon);
	}
	public void lose() {
		ImageIcon popupicon = new ImageIcon(TetrisMain.class.getResource("../../../Images/giphy-2.gif"));
		JOptionPane.showMessageDialog(null, null, "The End Loser", JOptionPane.ERROR_MESSAGE, popupicon);
	}
	  /**
	   * 라인을 지울때마다 gif 시각이미지가 출력되며, 1초뒤에 자동으로 사라짐.	
	   */
	public void removeLineImage() {
		new Thread() {   
            public void run() {   
                for (int k = 0; k < 1; k++) {   
                    try {   
                        Thread.sleep(2000);                               
                    } catch (InterruptedException e) {   
                    }   
                    JOptionPane.getRootFrame().dispose();   
                }   
            }   
        }.start();  
		ImageIcon popupicon = new ImageIcon(TetrisMain.class.getResource("../../../Images/BoomBitch!.gif"));
		JOptionPane.showMessageDialog(null, null, "IClearedALineBitch", JOptionPane.ERROR_MESSAGE, popupicon);
	}
	
	/**
	 * 고스트블럭을 보여준다.
	 */
	private void showGhost(){
		ghost = getBlockClone(shap,true);
		controllerGhost.setBlock(ghost);
		controllerGhost.moveQuickDown(shap.getPosY(), true);
	}	
	
	
	/**
	 * 랜덤으로 테트리스 블럭을 생성하고 반환한다.
	 * @return 테트리스 블럭
	 */
	public TetrisBlock getRandomTetrisBlock(){
		switch((int)(Math.random()*7)){
		case TetrisBlock.TYPE_CENTERUP : return new CenterUp(4, 1);
		case TetrisBlock.TYPE_LEFTTWOUP : return new LeftTwoUp(4, 1);
		case TetrisBlock.TYPE_LEFTUP : return new LeftUp(4, 1);
		case TetrisBlock.TYPE_RIGHTTWOUP : return new RightTwoUp(4, 1);
		case TetrisBlock.TYPE_RIGHTUP : return new RightUp(4, 1);
		case TetrisBlock.TYPE_LINE : return new Line(4, 1);
		case TetrisBlock.TYPE_NEMO : return new Nemo(4, 1);
		}
		return null;
	}
	
	
	/**
	 * tetrisBlock과 같은 모양으로 고스트의 블럭모양을 반환한다.
	 * @param tetrisBlock 고스트의 블럭모양을 결정할 블럭
	 * @return 고스트의 블럭모양을 반환
	 */
	public TetrisBlock getBlockClone(TetrisBlock tetrisBlock, boolean isGhost){
		TetrisBlock blocks = null;
		switch(tetrisBlock.getType()){
		case TetrisBlock.TYPE_CENTERUP : blocks =  new CenterUp(4, 1); break;
		case TetrisBlock.TYPE_LEFTTWOUP : blocks =  new LeftTwoUp(4, 1); break;
		case TetrisBlock.TYPE_LEFTUP : blocks =  new LeftUp(4, 1); break;
		case TetrisBlock.TYPE_RIGHTTWOUP : blocks =  new RightTwoUp(4, 1); break;
		case TetrisBlock.TYPE_RIGHTUP : blocks =  new RightUp(4, 1); break;
		case TetrisBlock.TYPE_LINE : blocks =  new Line(4, 1); break;
		case TetrisBlock.TYPE_NEMO : blocks =  new Nemo(4, 1); break;
		}
		if(blocks!=null && isGhost){
			blocks.setGhostView(isGhost);
			blocks.setPosX(tetrisBlock.getPosX());
			blocks.setPosY(tetrisBlock.getPosY());
			blocks.rotation(tetrisBlock.getRotationIndex());
		}
		return blocks;
	}	
	
	
	/**TODO : 콜백메소드
	 * 테트리스 블럭이 고정될 때 자동 호출 된다.
	 * @param removeCombo	현재 콤보 수
	 * @param removeMaxLine	한번에 지운 줄수 
	 */
	public void getFixBlockCallBack(ArrayList<Block> blockList, int removeCombo, int removeMaxLine){
		if(removeCombo<3){
			if(removeMaxLine==1)client.addBlock(1);
			else if(removeMaxLine==4)client.addBlock(3);
		}else if(removeCombo<10){
			if(removeMaxLine==3)client.addBlock(2);
			else if(removeMaxLine==4)client.addBlock(4);
			else client.addBlock(1);
		}else{
			if(removeMaxLine==3)client.addBlock(3);
			else if(removeMaxLine==4)client.addBlock(5);
			else client.addBlock(2);
		}
	}
	
	/**
	 * 블럭을 홀드시킨다.
	 */
	public void playBlockHold(){
		if(isHold) return;
		
		if(hold==null){
			hold = getBlockClone(shap,false);
			this.nextTetrisBlock();
		}else{
			TetrisBlock tmp = getBlockClone(shap,false);
			shap = getBlockClone(hold,false);
			hold = getBlockClone(tmp,false);
			this.initController();
		}
		
		isHold = true;
	}
	
	
	/**
	 * 가장 밑에 줄에 블럭을 생성한다.
	 * @param numOfLine
	 */
	boolean stop = false;
	public void addBlockLine(int numOfLine){
//		
//		 stop = true; // 내리기가 있을 때까지 대기한다. // 내리기를 모두 실행한 후 다시 시작한다. Block block; int
//		 rand = (int) (Math.random() * maxX); for (int i = 0; i < numOfLine; i++) {
//		 this.dropBoard(maxY - 1, -1); for (int col = 0; col < maxX; col++) { if (col
//		 != rand) { block = new Block(0, 0, Color.GRAY, Color.GRAY);
//		 block.setPosGridXY(col, maxY - 1); blockList.add(block); map[maxY - 1][col] =
//		 block; } } //만약 내려오는 블럭과 겹치면 블럭을 위로 올린다. boolean up = false; for(int j=0 ;
//		 j<shap.getBlock().length ; j++){ Block sBlock = shap.getBlock(j);
//		 if(map[sBlock.getY()][sBlock.getX()]!=null){ up = true; break; } } if(up){
//		 controller.moveDown(-1); } }
//		 
//		 
//		 
//		 
//		 this.showGhost(); this.repaint(); synchronized (this) { stop = false;
//		 this.notify(); }
//		 
	}
	
	public void GameEndPopUp() {
		GameEndSound =new Music("Synthetic_Insects",false);
		GameEndSound.start();    //게임 종료시음악 
		
	}// 게임 뮤직 키고 끄기 
	
	
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER){
			messageArea.requestFocus();
		}
		if(!isPlay) return;
		if(e.getKeyCode() == KeyEvent.VK_LEFT){
			if(usingEffect) new Music("cartoon008.mp3", false).start();
			controller.moveLeft();
			controllerGhost.moveLeft();
		}else if(e.getKeyCode() == KeyEvent.VK_RIGHT){
			if(usingEffect) new Music("cartoon008.mp3", false).start();
			controller.moveRight();
			controllerGhost.moveRight();
		}else if(e.getKeyCode() == KeyEvent.VK_DOWN){
			if(usingEffect) new Music("cartoon008.mp3", false).start();
			controller.moveDown();
		}else if(e.getKeyCode() == KeyEvent.VK_UP){
			if(usingEffect) new Music("cartoon008.mp3", false).start();
			controller.nextRotationLeft();
			controllerGhost.nextRotationLeft();
		}else if(e.getKeyCode() == KeyEvent.VK_SPACE){
			if(usingEffect) new Music("cartoon008.mp3", false).start();
			controller.moveQuickDown(shap.getPosY(), true);
			this.fixingTetrisBlock();
		}else if(e.getKeyCode() == KeyEvent.VK_SHIFT){ 
			playBlockHold();
		}
		this.getClient().drawBlockShap(controller.getBlock());//HK
		this.getClient().drawBlockDeposit(blockList);//HK
		this.getClient().drawBlockHold(hold);//HK
		this.getClient().drawBlockNext(nextBlocks);//HK
		this.showGhost();
		this.repaint();
	}

	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		this.requestFocus();
	}
	public void mouseReleased(MouseEvent e) {}
	
	// 게임 뮤직 키고 끄기
	public void actionPerformed1(ActionEvent e) {
		if (e.getSource() == btnStart) {
			
			if(GameMusic != null && GameMusic.isAlive()) {
				GameMusic.close();
				if(usingBGM) {
					GameMusic = new Music("Tetris.mp3", true );
					GameMusic.start();
					
					}
			}else {
				if(usingBGM) {
					GameMusic = new Music("Tetris.mp3", true );
					GameMusic.start();
					
				}
			}
				
			if(client!=null){
				client.gameStart((int)comboSpeed.getSelectedItem());
			}else{
				this.gameStart((int)comboSpeed.getSelectedItem());
			}
		}else if(e.getSource() == btnExit){
			if(GameMusic != null && GameMusic.isAlive()) {
				GameMusic.close();
			}
			if (client != null) {
				if (tetris.isNetwork()) {
					client.closeNetwork(tetris.isServer());
				}
			} else {
				System.exit(0);
			}

		}
	}
	

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btnStart){
			if(client!=null){
				client.gameStart((int)comboSpeed.getSelectedItem());
			}else{
				this.gameStart((int)comboSpeed.getSelectedItem());
			}
		}else if(e.getSource() == btnExit){
			if(client!=null ){
				if(tetris.isNetwork()){
					client.closeNetwork(tetris.isServer());
				}
			}else{
				System.exit(0);
			}
			
		}
	}
	
	
	public JComboBox<Integer> getComboSpeed() {return comboSpeed;}// HK
	public void setComboSpeed(JComboBox<Integer> comboSpeed) {this.comboSpeed = comboSpeed;}// HK
	public void setNext(ArrayList<TetrisBlock> nextBlocks2) {this.nextBlocks2 = nextBlocks2;}// HK
	public void setHold(TetrisBlock hold) {this.hold2 = hold;}// HK
	public void setDeposit(ArrayList<Block> blockList2) {this.blockList2 = blockList2;}// HK
	public void setShap(TetrisBlock shap) {this.shap2 = shap;}// HK
	public boolean isPlay(){return isPlay;}
	public void setPlay(boolean isPlay){this.isPlay = isPlay;}
	public JButton getBtnStart() {return btnStart;}
	public JButton getBtnExit() {return btnExit;}
	public void setClient(GameClient client) {this.client = client;}
	public void printSystemMessage(String msg){systemMsg.printMessage(msg);}
	public void printMessage(String msg){messageArea.printMessage(msg);}
	public GameClient getClient(){return client;}
	public void changeSpeed(Integer speed) {comboSpeed.setSelectedItem(speed);}
	public void clearMessage() {
		messageArea.clearMessage();
		systemMsg.clearMessage();
	}

}
