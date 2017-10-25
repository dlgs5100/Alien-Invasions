import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

class ExtraCharacter extends Character {
	public ExtraCharacter(String fname, int n_frames, int[] ds) {
		super(fname, n_frames, ds);
	}

	protected void updateCurFrameIdx(long elapsedTime) {		//爆炸Frame
		long t = elapsedTime + lastRemainTime;
		while (t > durs[curFrame]) {
			t -= durs[curFrame];
			curFrame++;
			if (curFrame >= durs.length) {
				curFrame = -1;
				valid = false;
				break;
			}
		}
		lastRemainTime = t;
	}
}

public class GameEngine extends Application {
	Character ship;
	Character bkg;
	Character UFO;
	AnchorPane pane;
	ArrayList<Character> aliens = new ArrayList<Character>();
	ArrayList<Character> bullets = new ArrayList<Character>();
	ArrayList<Character> bombs = new ArrayList<Character>();
	ArrayList<Character> bigbombs = new ArrayList<Character>();
	ArrayList<Character> flames = new ArrayList<Character>();
	ArrayList<Character> hearts = new ArrayList<Character>();
	ArrayList<Character> bossblood = new ArrayList<Character>();
	double accSpeed = 0.0;
	final double BASE_SPEED = 50;
	long lastTime, curTime, elapsedTime;
	int BombTime = 0, AlienRange = 10, last = 0, life = 3, AttackCount = 0, GapBomb = 0, Bosslife = 7;
	boolean Boss = false, mode = true;
	URL url_BGM = getClass().getResource("BGM.mp3"); 
	AudioClip aclip_BGM = new AudioClip(url_BGM.toString());

	public void updateAll(long elapsedTime) {					//更新所有Frame
		double w = bkg.getFrame().getWidth();
		bkg.update(elapsedTime);
		ship.update(elapsedTime);
		if(AlienRange == 0) {					//小兵死光，Boss動作
			UFO.update(elapsedTime);
			double x = UFO.getX();
			if (x >= (w - UFO.getFrame().getWidth()) || x <= 0){
				UFO.setVx(-UFO.getVx());
			}

			if (mode == true) {					//模式1: 丟大炸彈
				BombTime++;
				if (BombTime >= 80) {
					BossBombThrow(mode);
					BombTime = 0;
					AttackCount++;
				}
				if(AttackCount == 5){
					mode = !mode;
					AttackCount = 0;
				}
			}
			else if	(mode == false) {			//模式2: 丟小炸彈
				BombTime++;
				if (BombTime >= 10) {
					BossBombThrow(mode);
					BombTime = 0;
					AttackCount++;
				}
				if(AttackCount == 12){
					mode = !mode;
					AttackCount = 0;
				}
			}
		}
		else {									//小兵動作
			for (Character s : aliens) {
				s.update(elapsedTime);
				double x = s.getX();
				if (x >= (w - s.getFrame().getWidth()) || x <= 0)
					s.setVx(-s.getVx());
				BombTime++;
				if (BombTime >= 250) {			//炸彈間隔時間
					int choose = (int) (Math.random() * AlienRange);
					if (AlienRange != 1)		//剩下一個之外，不會一直是同一個在攻擊
						while (choose == last)
							choose = (int) (Math.random() * AlienRange);

					int speed = (int) (Math.random() * (7 - 3 + 1) + 3);

					last = choose;
					BombThrow(choose, speed);
					BombTime = 0;
				}
			}
		}
		for (Character s : bullets) {
			s.update(elapsedTime);
		}
		for (Character s : bombs) {
			s.update(elapsedTime);
		}
		for (Character s : bigbombs) {
			s.update(elapsedTime);
		}
		for (Character s : flames) {
			if (s.curFrame >= 0)
				s.update(elapsedTime);
		}		
	}

	AnimationTimer tm = new AnimationTimer() {				//Timer控制所有時間
		@Override
		public void handle(long arg0) {
			if(!aclip_BGM.isPlaying())
				aclip_BGM.play();
			
			if (AlienRange == 0 && Boss == false){
				initBoss();
				Boss = true;
			}
			
			curTime = System.currentTimeMillis();
			elapsedTime = curTime - lastTime;
			updateAll(elapsedTime);
			reclaimCharacters();
			lastTime = curTime;
			try {
				Thread.sleep((long) (10));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (life == 0) {				//失敗
				tm.stop();
				Text t = new Text("Defeat!!");
				t.setStyle("-fx-font-size: 50px;");
				t.setFill(Color.WHITE);
				AnchorPane.setBottomAnchor(t, bkg.getHeight()/2);
				AnchorPane.setRightAnchor(t, bkg.getWidth()/2-90);
				pane.getChildren().add(t);
				
				aclip_BGM.stop();
				URL url = getClass().getResource("Defeat.mp4"); 
				AudioClip aclip = new AudioClip(url.toString());
				aclip.play();
			}
			else if (Bosslife == 0){		//獲勝
				tm.stop();
				Text t = new Text("Victory!!");
				t.setStyle("-fx-font-size: 50px;");
				t.setFill(Color.WHITE);
				AnchorPane.setBottomAnchor(t, bkg.getHeight()/2);
				AnchorPane.setRightAnchor(t, bkg.getWidth()/2-90);
				pane.getChildren().add(t);
				
				aclip_BGM.stop();
				URL url = getClass().getResource("Victory.mp3"); 
				AudioClip aclip = new AudioClip(url.toString());
				aclip.play();
			}
		}
	};
	private void initBoss() {
		
		UFO.setVx(BASE_SPEED*5);
		Character s = new Character("blood7", 1, new int[] { 10 });				//Boss血條初始化
		s.setPos(10 , 10);
		bossblood.add(s);
		pane.getChildren().add(s.getView());
		for(int i=6; i>0; i--){
			s = new Character("blood"+Integer.toString(i), 1, new int[] { 10 });
			s.setPos(10 , 10);
			bossblood.add(s);
		}
	}
	private void initCharacters() {
		bkg = new Character("bg_image", 1, new int[] { 10 });
		bkg.setBnd(0, bkg.getFrame().getWidth(), 0, bkg.getFrame().getHeight());
		pane.getChildren().add(bkg.getView());
		ship = new Character("spaceship", 1, new int[] { 10 });
		ship.setBnd(0, bkg.getFrame().getWidth(), 0, bkg.getFrame().getHeight());
		ship.setVx(BASE_SPEED);
		ship.setPos(ship.getX(), bkg.getFrame().getHeight() - ship.getFrame().getHeight());
		pane.getChildren().add(ship.getView());
		UFO = new Character("UFO", 1, new int[] { 10 });
		UFO.setBnd(0, bkg.getFrame().getWidth(), 0, bkg.getFrame().getHeight());
		UFO.setPos(bkg.getFrame().getWidth()/2 - ship.getFrame().getWidth()*2, 50);
		pane.getChildren().add(UFO.getView());
		for (int n = 0; n < 10; n++) {
			int[] durs = new int[15];
			for (int i = 0; i < durs.length; i++)
				durs[i] = 50 + (int) (50 * Math.random() + 1);
			Character s = new Character("SkeltonFrame", 15, durs);
			s.setBnd(0, bkg.getFrame().getWidth(), 0, bkg.getFrame().getHeight());
			s.setVx(BASE_SPEED * (Math.random() + 1));
			s.setPos(n * 80, Math.random() * 300);
			aliens.add(s);
			pane.getChildren().add(s.getView());
		}
		for (int n = 0; n < life; n++) {
			Character s = new Character("heart", 1, new int[] { 10 });
			s.setBnd(0, bkg.getFrame().getWidth(), 0, bkg.getFrame().getHeight());
			s.setPos(bkg.getFrame().getWidth() - 100, 30 + 100 * n);
			hearts.add(s);
			pane.getChildren().add(s.getView());
		}
	}

	private void BombThrow(int choose, int speed) {
		Character bomb = new Character("bomb", 1, new int[] { 10 });
		bomb.posProperty().addListener((ev) -> checkBomb(bomb));
		bomb.setBnd(0, bkg.getFrame().getWidth(), 0, bkg.getFrame().getHeight());
		bomb.setVy(BASE_SPEED * speed);

		Character a = aliens.get(choose);
		double x = a.getX();
		double y = a.getY();
		bomb.setPos(x + a.getWidth() / 2, y - bomb.getHeight());
		pane.getChildren().add(bomb.getView());
		bombs.add(bomb);
	}
	private void BossBombThrow(boolean mode) {
		if (mode == true) {
			Character bomb = new Character("bigbomb", 1, new int[] { 10 });
			bomb.posProperty().addListener((ev) -> checkBomb(bomb));
			bomb.setBnd(0, bkg.getFrame().getWidth(), 0, bkg.getFrame().getHeight());
			bomb.setVy(BASE_SPEED * 7);

			Character a = UFO;
			double x = a.getX();
			bomb.setPos(x + a.getWidth() / 2, 100);
			pane.getChildren().add(bomb.getView());
			bigbombs.add(bomb);
		}
		else if (mode == false) {
			Character a = UFO;
			double x = a.getX();
			//兩排炸彈
			Character bomb1 = new Character("bomb", 1, new int[] { 10 });
			bomb1.posProperty().addListener((ev) -> checkBomb(bomb1));
			bomb1.setBnd(0, bkg.getFrame().getWidth(), 0, bkg.getFrame().getHeight());
			bomb1.setVy(BASE_SPEED * 6);	
			bomb1.setPos(x, 100);
			pane.getChildren().add(bomb1.getView());
			bigbombs.add(bomb1);
			
			Character bomb2 = new Character("bomb", 1, new int[] { 10 });
			bomb2.posProperty().addListener((ev) -> checkBomb(bomb2));
			bomb2.setBnd(0, bkg.getFrame().getWidth(), 0, bkg.getFrame().getHeight());
			bomb2.setVy(BASE_SPEED * 6);
			bomb2.setPos(x + a.getWidth(), 100);
			pane.getChildren().add(bomb2.getView());
			bigbombs.add(bomb2);
		}
	}

	private void checkBullet(Character b) {
		if (b.getY() < 0)
			b.valid = false;
		else if (aliens.size() > 0) {
			for (Iterator<Character> it = aliens.iterator(); it.hasNext();) {
				Character a = it.next();
				if (a.collideWith(b)) {
					double x = a.getX() + a.getWidth() / 2;
					double y = a.getY() + a.getHeight() / 2;
					a.valid = b.valid = false;
					Character flame = new ExtraCharacter("ExplodeFrame", 9,
							new int[] { 100, 100, 100, 100, 100, 100, 100, 100, 100 });
					flame.setBnd(0, bkg.getFrame().getWidth(), 0, bkg.getFrame().getHeight());
					flame.setPos(x, y);
					pane.getChildren().add(flame.getView());
					flames.add(flame);
					AlienRange--;
					
					URL url = getClass().getResource("AlienSound.mp3"); 
					AudioClip aclip = new AudioClip(url.toString());
					aclip.play();
				}
			}
		}
	}
	private void BosscheckBullet(Character b) {
		if (b.getY() < 0)
			b.valid = false;
		else if (UFO.valid){
			Character a = UFO;
			if (a.collideWith(b)) {
				double x = a.getX() + a.getWidth() / 2;
				double y = a.getY() + a.getHeight() / 2;
				b.valid = false;
				if(Bosslife == 0)
					a.valid = false;
				Character flame = new ExtraCharacter("ExplodeFrame", 9,
						new int[] { 100, 100, 100, 100, 100, 100, 100, 100, 100 });
				flame.setBnd(0, bkg.getFrame().getWidth(), 0, bkg.getFrame().getHeight());
				flame.setPos(x, y);
				pane.getChildren().add(flame.getView());
				flames.add(flame);
				//更換血條圖
				Character c = bossblood.get(0);
				c.valid = false;
				Bosslife--;
				if (Bosslife > 0) {
					Character s = bossblood.get(1);
					pane.getChildren().add(s.getView());
				}
				
				URL url = getClass().getResource("AlienSound.mp3"); 
				AudioClip aclip = new AudioClip(url.toString());
				aclip.play();
			}

		}
	}

	private void checkBomb(Character b) {
		if (b.getY() < 0)
			b.valid = false;
		else {
			Character a = ship;
			if (a.collideWith(b)) {
				double x = a.getX() + a.getWidth() / 2;
				double y = a.getY() + a.getHeight() / 2;
				b.valid = false;
				Character flame = new ExtraCharacter("ExplodeFrame", 9,
						new int[] { 100, 100, 100, 100, 100, 100, 100, 100, 100 });
				flame.setBnd(0, bkg.getFrame().getWidth(), 0, bkg.getFrame().getHeight());
				flame.setPos(x, y);
				pane.getChildren().add(flame.getView());
				flames.add(flame);

				life--;
				hearts.get(life).valid = false;
				
				URL url = getClass().getResource("BoomSound.mp3"); 
				AudioClip aclip = new AudioClip(url.toString());
				aclip.play();
			}
		}
	}

	private void reclaimCharacters() {
		for (Iterator<Character> it = bullets.iterator(); it.hasNext();) {
			Character b = it.next();
			if (!b.valid) {
				pane.getChildren().remove(b.iv);
				it.remove();
			}
		}
		for (Iterator<Character> it = aliens.iterator(); it.hasNext();) {
			Character a = it.next();
			if (!a.valid) {
				pane.getChildren().remove(a.iv);
				it.remove();
			}
		}
		for (Iterator<Character> it = flames.iterator(); it.hasNext();) {
			Character f = it.next();
			if (!f.valid) {
				pane.getChildren().remove(f.iv);
				it.remove();
			}
		}
		for (Iterator<Character> it = bombs.iterator(); it.hasNext();) {
			Character b = it.next();
			if (!b.valid) {
				pane.getChildren().remove(b.iv);
				it.remove();
			}
		}
		for (Iterator<Character> it = bigbombs.iterator(); it.hasNext();) {
			Character b = it.next();
			if (!b.valid) {
				pane.getChildren().remove(b.iv);
				it.remove();
			}
		}
		for (Iterator<Character> it = hearts.iterator(); it.hasNext();) {
			Character h = it.next();
			if (!h.valid) {
				pane.getChildren().remove(h.iv);
				it.remove();
			}
		}
		for (Iterator<Character> it = bossblood.iterator(); it.hasNext();) {
			Character b = it.next();
			if (!b.valid) {
				pane.getChildren().remove(b.iv);
				it.remove();
			}
		}
	}

	private void keyStrike(KeyEvent e) {
		accSpeed = BASE_SPEED * 2.0;
		if (e.getCode() == KeyCode.LEFT && ship.getVx() < 0) {
			ship.setVx(ship.getVx() - accSpeed);
		} else if (e.getCode() == KeyCode.RIGHT && ship.getVx() > 0) {
			ship.setVx(ship.getVx() + accSpeed);
		} else if (e.getCode() == KeyCode.LEFT)
			ship.setVx(-BASE_SPEED);
		else if (e.getCode() == KeyCode.RIGHT)
			ship.setVx(BASE_SPEED);
		if (e.getCode() == KeyCode.SPACE) {
			if (bullets.size() < 2) {
				Character bullet = new Character("bullet", 1, new int[] { 10 });
				bullet.posProperty().addListener((ev) -> checkBullet(bullet));
				if(AlienRange == 0)
					bullet.posProperty().addListener((ev) -> BosscheckBullet(bullet));
				bullet.setBnd(0, bkg.getFrame().getWidth(), 0, bkg.getFrame().getHeight());
				bullet.setVy(-BASE_SPEED * 8);
				double x = ship.getX();
				double y = ship.getY();
				bullet.setPos(x + ship.getWidth() / 2, y - bullet.getHeight());
				pane.getChildren().add(bullet.getView());
				bullets.add(bullet);
				

				URL url = getClass().getResource("BulletSound.wav"); 
				AudioClip aclip = new AudioClip(url.toString());
				aclip.play();
			}
		}
	}

	@Override
	public void start(Stage primaryStage) {
		pane = new AnchorPane();
		pane.setOnKeyPressed(e -> keyStrike(e));
		initCharacters();
		Scene scene = new Scene(pane, bkg.getWidth(), bkg.getHeight());
		primaryStage.setScene(scene);
		primaryStage.show();
		pane.requestFocus();
		lastTime = System.currentTimeMillis();
		tm.start();
	}

	public static void main(String[] args) {
		launch();
	}
}