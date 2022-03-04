package com.GenSci.tools.GrahpViewer;

import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
//import javafx.embed.swing.SwingFXUtils;

public class PrimaryController implements Initializable {
	// private static final double RADIUS = 10_000f;
	// private DoubleProperty scale = new SimpleDoubleProperty(1.0);
	@FXML
	Button openAveBtn;
	@FXML
	Button openTypeBtn;
	@FXML
	Button drawAveBtn;
	@FXML
	Button drawTypeBtn;
	@FXML
	Button saveAveBtn;
	@FXML
	Button saveTypeBtn;
	@FXML
	TextArea log;
	@FXML
	Button quitBtn;
	@FXML
	Canvas aveCanvas;
	@FXML
	Canvas aveLineCanvas;
	@FXML
	Canvas typeLineCanvas;
	@FXML
	Canvas typeCanvas;
	@FXML
	private Spinner<Integer> expSpinner;
	@FXML
	Slider genSlider;
	@FXML
	TextField genTextField;
	@FXML
	Label aveValueLabel;
	//
	GraphicsContext gAve; // 平均値描画
	GraphicsContext gType; // タイプ別比率描画
	GraphicsContext gAveLine; // 平均値世代線
	GraphicsContext gTypeLine; // タイプ別比率世代線
	//
	// ファイルが読まれた後から設定されてしまうパラメータ
	int GEN; // 世代数
	int EXP; // 実験数。
	int nowGen;// 現在の世代
	int nowExp = 0; // 現在の実験番号。これは expSpinner で変更される。
	double[][] aveDataTable;
	double[][] typeDataTable;
	double[] nowExpAveData; // 実験番号が定まった後から中身が入る平均値
	double[][] nowExpTypeData; // 実験番号が定まった後から中身が入るタイプ別個体比率
	//

	//
	double width, height;// canvas の横幅と高さ
	// グラフエリアの上限下限・左右
	int top, bottom, left, right;
	// グラフエリアの高さ・幅
	int gHeight, gWidth;
	// グラフエリアの中のマージン
	int margin = 30;
	boolean aveDataFlag = false; // 平均データを読み込んだら true にする。
	String dir = null; // データのディレクトリ

	//
	List<String> aveDataStr = new ArrayList<String>();// ファイルから読み込んだ平均値テーブル
	List<String> typeDataStr = new ArrayList<String>();// ファイルから読み込んだタイプ別個体比率テーブル
	//
	//

	//
	@FXML
	void quitAction() {
		System.exit(0);
	}

	//
	public void draw(GraphicsContext g, int[] data) {
		// 与えられたデータを与えられたグラフィックスに描く
	}

	//
	public void execAction() {

	}

	//
	public void openAveFile() {
		openAction(aveDataStr);
		// 1行取り出す
		String str = aveDataStr.get(0);
		// log.appendText(str + "\n");
		String[] row = str.split("\t");
		// log.appendText(row.length + "\tgen=" + aveDataStr.size() + "\n");
		//
		EXP = row.length;
		GEN = aveDataStr.size();
		// データを double配列にしまい込む。
		aveDataTable = new double[GEN][EXP];
		for (int i = 0; i < GEN; i++) {
			str = aveDataStr.get(i); // 一行読み込んだ
			row = str.split("\t"); // tab 区切りで列を分けた
			for (int j = 0; j < EXP; j++) {
				aveDataTable[i][j] = Double.parseDouble(row[j]);
			}
		} // end of for( double 配列にしまいこむ
		aveDataFlag = true;
		//

		// ここから平均値グラフの表示
		// グラフの軸を作りたい。
		// 横軸（世代軸）は2つのCanvasで共通なのでgraphicContext を渡して作成する。
		drawXAxis(gAve);

		// double 配列にデータが入ったので、スピナーから実験番号を読んでその列を別の配列に移す。
		SpinnerValueFactory<Integer> valueFactory1 = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, EXP - 1, 1);
		expSpinner.setValueFactory(valueFactory1);
		valueFactory1.setWrapAround(true);
		valueFactory1.setValue(0);

		nowExp = expSpinner.getValue();
		// Table から抜き出した配列。
		nowExpAveData = new double[GEN];
		for (int i = 0; i < GEN; i++) {
			nowExpAveData[i] = aveDataTable[i][nowExp];
			// log.appendText(nowExpAveData[i]+"\n");
		}
		// strokPolyLine に渡すための pxel 配列
		double[] yPix = new double[GEN];
		double[] xPix = new double[GEN];
		// データが抜き出されたのでpixelデータを作る
		makePixelData(xPix, yPix, nowExpAveData, gWidth, gHeight);
		// 描画。polyLine を使いたい。
		gAve.strokePolyline(xPix, yPix, GEN);
		// change Listener をつけて実験回数を spinner で変更するたびにグラフを更新する。
		expSpinner.valueProperty().addListener(new ChangeListener<Integer>() {
			@Override
			public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
				nowExp = expSpinner.getValue();
				log.appendText("exp = " + nowExp + "\n");
				for (int i = 0; i < GEN; i++) {
					nowExpAveData[i] = aveDataTable[i][nowExp];
				}
				// データが抜き出されたのでpixelデータを作る
				makePixelData(xPix, yPix, nowExpAveData, gWidth, gHeight);
				// 描画。polyLine を使いたい。
				gAve.clearRect(0, 0, width, height);
				gAve.setFill(Color.WHITE);
				gAve.fillRect(0, 0, width, height);
				drawXAxis(gAve);
				gAve.strokePolyline(xPix, yPix, GEN);
			}
		});
		//
		// スライダーによる世代を示す赤線をかぶせる。
		genSlider.setMax(GEN-1);
		genSlider.setMajorTickUnit(GEN / 10.0);
		genSlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				nowGen = newValue.intValue();
				genTextField.setText("" + nowGen);
				double coeff = gWidth / GEN;
				int xPos = (int) (coeff * nowGen) + margin;
				if (nowGen != 0 && nowGen != GEN) {
					gAveLine.clearRect(0, 0, width, height);
					gAveLine.setStroke(Color.RED);
					gAveLine.strokeLine(xPos, 0, xPos, height);
					gTypeLine.clearRect(0, 0, width, height);
					gTypeLine.setStroke(Color.RED);
					gTypeLine.strokeLine(xPos, 0, xPos, height);
				} else {
					gAveLine.clearRect(0, 0, width, height);
					gTypeLine.clearRect(0, 0, width, height);
				}
				//赤線上の平均値を Label に表示する。
				double v = nowExpAveData[nowGen];
				String str = new String(""+v);
				aveValueLabel.setText(str);
			} // end of changed()

		});
	} // end of openAveFile()

	// x軸、y軸のpixelデータを作成する。
	public void makePixelData(double[] xPix, double[] yPix, double[] yData, int w, int h) {
		// x軸方向についてはこのメソッドの中で作ってしまうので引数に元データはない。
		double[] y = translate(h, yData);
		for (int i = 0; i < GEN; i++) {
			double d = y[i];
			d = d + margin;
			y[i] = d;
		} // end of for(yPix の margin 調整
		for (int i = 0; i < GEN; i++) {
			// このように引数配列に直接値を入れないと渡した側の配列が変わらない。なぜ？
			yPix[i] = y[i];
		}
		double par = w / GEN;
		xPix[0] = 0.0;
		for (int i = 1; i < GEN; i++) {
			xPix[i] = xPix[i - 1] + par;
		}
		for (int i = 0; i < GEN; i++) {
			double d = xPix[i];
			d = d + margin;
			xPix[i] = d;
		}
		//
	} // end of makePixelData()

	// translate pixel 幅と double配列を与えられて、pixel値の配列を返す。
	public double[] translate(int height, double[] data) {
		// canvas.strokPolyLine() がdouble[] をとるので。
		double[] d = new double[data.length];
		double maxValue = 3.0;
		// 数値1.0あたりの pixel数
		double par = gHeight / maxValue;
		// log.appendText("height=" + height + "par=" + par + "\n");
		// 配列r に pixel換算されたデータが入るが、グラフの座標は top が0なので
		// pixelとしては数値の最大が高さ座標は0。したがって、あらかじめ
		// データの値をすべて最大値からひいたものにしておく。
		for (int i = 0; i < data.length; i++) {
			d[i] = par * (maxValue - data[i]);
		}
		return d;
	}

	// x軸を作る
	public void drawXAxis(GraphicsContext g) {
		g.strokeLine(left, bottom, right, bottom);
		int tickTop = bottom;
		int tickBottom = tickTop + 10;
		// 世代数の長さに応じて、目盛りが違う。
		// 1世代あたりpixel値
		int par = (int) (gWidth / GEN);
		//log.appendText("par = " + par + "gWidth= " + gWidth + "\t" + (right - left) + "\n");
		int dist = left;
		for (int i = 0; i < GEN; i++) {
			String str = new String("" + i);
			if ((i % 10) == 0) {
				g.strokeText(str, dist, tickBottom);
			}
			dist += par;
		}
		String str = new String("" + (GEN - 1));
		g.strokeText(str, dist, tickBottom);
	}

	public void openTypeFile() {
		openAction(typeDataStr);
	}

	//
	public void openAction(List<String> list) {
		FileChooser fc = new FileChooser();
		if (dir != null) {
			fc.setInitialDirectory(new File(dir));
		}
		fc.setTitle("open data file");
		File file = fc.showOpenDialog(null);
		dir = file.getAbsolutePath();
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line = null;
			while ((line = br.readLine()) != null) {
				list.add(line);
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}// end of openAction();
		//

	public void saveAction() {

		FileChooser savefile = new FileChooser();
		savefile.setTitle("Save File");

		File file = savefile.showSaveDialog(null);
		System.out.println("is file null ? " + file);
		if (file != null) {
			WritableImage writableImage = new WritableImage((int) width, (int) height);
			aveCanvas.snapshot(null, writableImage);
			RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
			try {
				ImageIO.write(renderedImage, "png", file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// グラフィックコンテキストの確定
		gAve = aveCanvas.getGraphicsContext2D();
		gType = typeCanvas.getGraphicsContext2D();
		gAveLine = aveLineCanvas.getGraphicsContext2D();
		gTypeLine = typeLineCanvas.getGraphicsContext2D();
		//
		// Canvas size の取得。すべてのCanvasで同じにしておく。のは難しいので SceneBuilder で設定。
		width = aveCanvas.getWidth();
		height = aveCanvas.getHeight();
		//
		gAve.setFill(Color.WHITE);
		gAve.fillRect(0, 0, width, height);
		gType.setFill(Color.WHITE);
		gType.fillRect(0, 0, width, height);
		// グラフエリアの上限下限・左右
		top = margin;
		bottom = (int) height - margin;
		left = margin;
		right = (int) width - margin;
		gHeight = bottom - top; // グラフ領域高さのpixel が値 0.0 - 3.0
		gWidth = right - left; // グラフ領域幅の pixel値
//		//check
//		Font font = new Font("Arial",12);
//		gAve.setFont(font);
//		gAve.strokeText("Here", 500, 500);
////		gAve.setStroke(Color.BLACK);
//		gAve.strokeLine(left, top, right, bottom);
//		expSpinner.valueProperty().addListener(new ChangeListener<Integer>() {
//			@Override
//			public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
//				// TODO Auto-generated method stub
//
//			}
//
//		});

	}
}
