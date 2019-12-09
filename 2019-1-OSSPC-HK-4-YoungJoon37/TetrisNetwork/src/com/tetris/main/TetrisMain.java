/**
 * Tetris 
 * 
 * 	ver 0.0.0 : 테트리스에서 사용할 블럭제작
 * 				Block, TetrisBlock, CenterUp, LeftTwoUp, LeftUp, Line, Nemo, RightTwoUp, RightUp
 *  
 *  ver 0.1.0 : 블럭을 움직이게 하는 컨트롤러 제작
 *  			TetrisController
 *  
 *  ver 0.2.0 : 맵 추가 
 *  			블럭 움직임 제한(벽에서 회전, 왼쪽이동 제한, 오른쪽이동 제한, 아래이동 제한)
 *  			바로 내리기 추가, 고스트 블럭 추가
 *  
 *  err 0.2.0 : [해결] 테트리스 블럭의 높이가 1일 경우, 회전이 되지 않음
 *  
 *  ver 0.2.1 : err 0.2.0 해결
 *  
 *  ver 0.3.0 : 블럭이 쌓기 처리
 *  			블럭 한줄 증가 처리
 *  			블럭 지우기 처리
 *  
 *  err 0.3.0 : [해결] 블럭 쌓기 시, 맵과 다른 좌표로 index에러발생 
 *  
 *  ver 0.3.1 : err 0.3.0 해결
 *  
 *  ver 1.0.0 : 서버 및 클라이언트의 기본틀 제작
 *  
 *  ver 1.1.0 : 네트워크의 명령어를 추가 (게임을 시작, 종료, 강제종료, 접속끊기 등)
 *  
 *  err 1.1.0 : [해결] 1. 네트워크 플레이 중, 공격에 따른 라인증가와 내리기의 checkIndex 충돌.
 *  			[해결] 2. LeftUp 블럭을 회전시켜 왼쪽으로 빠르게 이동시키면, 맵의 상단부분에서 나가짐.
 *  
 *  ver 1.1.1 : err 1.1.0 중 2해결
 * 	
 *  ver 1.2.0 : 테트리스 공격라인 벨런스 조정.{(0,1,3,),(1,3,5),(2,4,7)}  -->  {(0,1,3,),(1,2,4),(2,3,5)}
 *  
 *  ver 1.2.1 : 테트리스 블럭 색상 변경
 *  			고스트모드 on/off 기능 추가
 *  			그리드모드 on/off 기능 추가
 *  
 *  ver 1.3.0 : TotalAdd 추가
 *  			err 1.1.0 중 1해결
 *  
 *  ver 1.3.1 : 클라이언트 입장시 플레이어 번호 작은 순으로 입장.
 *  
 *  err 1.3.1 : [해결]서버로 접속 후 다른 클라이언트 접속,  서버측 x로 빠져나가면 에러발생
 *  
 *  ver 1.3.2 : err 1.3.1 해결
 *  
 *  최종버전 - ver 1.3.2
 */

package com.tetris.main;

import com.tetris.window.Tetris;


public class TetrisMain{
	public static void main(String[] args){
		new Tetris();
	}
}


