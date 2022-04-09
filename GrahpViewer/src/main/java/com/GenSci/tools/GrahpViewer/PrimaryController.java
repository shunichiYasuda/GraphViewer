package com.GenSci.tools.GrahpViewer;

import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

public class PrimaryController implements Initializable {
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
	Button saveAll01Btn;
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
	Canvas all01Canvas;
	@FXML
	private Spinner<Integer> expSpinner;
	@FXML
	Slider genSlider;
	@FXML
	TextField genTextField;
	//
	@FXML
	Label aveValueLabel;
	@FXML
	private CheckBox cb_All01;
	//
	Color[] typeColor; // type別グラフの色
	@FXML
	Label lb0; // あまのじゃくの数
	@FXML
	Label lb1; // お人好しの数
	@FXML
	Label lb2; // 裏切り者の数
	@FXML
	Label lb3; // 互恵主義者の数
	@FXML
	Label lb4; //All 0（記憶領域がすべて0：協力）の数
	@FXML
	Label lb5; //All 1（記憶領域がすべて0：裏切り）の数
	// ラベルに枠線を付けて色分けし、わかりやすくする。
	Border[] border;
	//
	GraphicsContext gAve; // 平均値描画
	GraphicsContext gType; // タイプ別比率描画
	GraphicsContext gAveLine; // 平均値世代線
	GraphicsContext gTypeLine; // タイプ別比率世代線
	GraphicsContext gAll01;
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
	double[][] nowExpAll01Data; // 実験番号が定まった後から中身が入る All 0,1 個体比率。
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
	boolean typeDataFlag = false;
	String dir = null; // データのディレクトリ
	String dateName; // 保存ファイルにつける日時

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

	// checkbox action
	public void checkBtnCange(ActionEvent event) {
		if (!typeDataFlag)
			return;
		if (cb_All01.isSelected()) {
			// System.out.println("selected");
			all01Canvas.toFront();
			typeLineCanvas.toFront();
		} else {
			// System.out.println("unselected");
			typeCanvas.toFront();
			typeLineCanvas.toFront();
		}
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
		// 縦軸も共通だが、最大値が異なるため最大値も渡す。
		drawYAxis(gAve, 3.0);
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
		makePixelData(xPix, yPix, nowExpAveData, gWidth, gHeight, 3.0);
		// 描画。polyLine を使いたい。
		gAve.strokePolyline(xPix, yPix, GEN);
		// change Listener をつけて実験回数を spinner で変更するたびにグラフを更新する。
		expSpinner.valueProperty().addListener(new ChangeListener<Integer>() {
			@Override
			public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
				nowExp = expSpinner.getValue();
				// log.appendText("exp = " + nowExp + "\n");
				for (int i = 0; i < GEN; i++) {
					nowExpAveData[i] = aveDataTable[i][nowExp];
				}
				// データが抜き出されたのでpixelデータを作る
				makePixelData(xPix, yPix, nowExpAveData, gWidth, gHeight, 3.0);
				// 描画。polyLine を使いたい。
				gAve.clearRect(0, 0, width, height);
				gAve.setFill(Color.WHITE);
				gAve.fillRect(0, 0, width, height);
				drawXAxis(gAve);
				drawYAxis(gAve, 3.0);
				gAve.strokePolyline(xPix, yPix, GEN);
			}
		});
		//
		// スライダーによる世代を示す赤線をかぶせる。
		genSlider.setMax(GEN - 1);
		genSlider.setMajorTickUnit(GEN / 10.0);
		genSlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				nowGen = newValue.intValue();
				genTextField.setText("" + nowGen);
				double coeff = gWidth / GEN;
				int xPos = (int) (coeff * nowGen) + margin;
				if (nowGen != 0 && nowGen != GEN - 1) {
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
				// 赤線上の平均値を Label に表示する。
				double v = nowExpAveData[nowGen];
				String str = new String("" + v);
				aveValueLabel.setText(str);
				// もしtypeファイルが開かれていたら、それぞれの数値をラベルに表示
				if (typeDataFlag) {
					v = nowExpTypeData[nowGen][0];
					str = new String("" + v);
					lb0.setText(str);
					v = nowExpTypeData[nowGen][1];
					str = new String("" + v);
					lb1.setText(str);
					v = nowExpTypeData[nowGen][2];
					str = new String("" + v);
					lb2.setText(str);
					v = nowExpTypeData[nowGen][3];
					str = new String("" + v);
					lb3.setText(str);
					v= nowExpAll01Data[nowGen][0];
					str = new String("" + v);
					lb4.setText(str);
					v= nowExpAll01Data[nowGen][1];
					str = new String("" + v);
					lb5.setText(str);
				}
			} // end of changed()

		});
	} // end of openAveFile()

	// x軸、y軸のpixelデータを作成する。
	public void makePixelData(double[] xPix, double[] yPix, double[] yData, int w, int h, double maxValue) {
		// x軸方向についてはこのメソッドの中で作ってしまうので引数に元データはない。
		double[] y = translate(h, yData, maxValue);
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
	public double[] translate(int height, double[] data, double maxValue) {
		// canvas.strokPolyLine() がdouble[] をとるので。
		double[] d = new double[data.length];
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
		// log.appendText("par = " + par + "gWidth= " + gWidth + "\t" + (right - left) +
		// "\n");
		int dist = left;
		for (int i = 0; i < GEN; i++) {
			String str = new String("" + i);
			if ((i % 10) == 0) {
				g.strokeText(str, dist, tickBottom);
			}
			dist += par;
		}
//		String str = new String("" + (GEN - 1));
//		g.strokeText(str, dist, tickBottom);
	}

	// y軸を作る。データによって最大値が異なるため、最大値を double で与える。
	public void drawYAxis(GraphicsContext g, double maxValue) {
		// まずは直線
		g.strokeLine(left, bottom, left, top);
		// 目盛りをつくる。
		// 目盛りの数値。分割数5
		int N = 5;
		double part = maxValue / (double) N;
		// 目盛り配列を作っておいた方が楽.
		// 0から最大値まで入るので分割数+1の配列が筆王
		double[] tickValue = new double[N + 1];
		for (int i = 0; i < tickValue.length; i++) {
			tickValue[i] = part * i;
			// log.appendText(tickValue[i]+"\n");
		}
		// 上で作ったのは0.0 からmaxValue まで順に並んだ刻みの数値。
		// 1.0 あたりpixel値
		double tmp = gHeight / maxValue;
		// 表示する画面上の位置は一番上が0なのでposition は最大値から刻みの値を引いた
		// 数値を入れなければならない
		double[] pos = new double[N + 1];
		for (int i = 0; i < pos.length; i++) {
			pos[i] = (maxValue - tickValue[i]) * tmp + margin;
		}
		// 表示する数値は綺麗にまるめて、Stringである。
		String[] str = new String[pos.length];
		for (int i = 0; i < tickValue.length; i++) {
			double d = round(tickValue[i], 1);// 小数点以下1ケタ
			str[i] = new String("" + d);
		}
		// 表示する。
		for (int i = 0; i < tickValue.length; i++) {
			g.strokeText(str[i], left - 18, pos[i]);
		}

	}

	public void openTypeFile() {
		openAction(typeDataStr);
		// 1行取り出す
		String str = typeDataStr.get(0);
		// log.appendText(str + "\n");
		String[] row = str.split("\t");
		// System.out.println("data columns =" + row.length);
		// log.appendText(row.length + "\tgen=" + typeDataStr.size() + "\n");
		// EXPとGENは平均値ファイルを読んだときに決められる。ファイルの形式が異なるので
		// type ファイルから決めてはダメ。
		// データを double配列にしまい込む。このとき、typeDataTable は
		// 「全実験」のデータを行に持つので、行数は GENではない。
		typeDataTable = new double[GEN * EXP][row.length];
		for (int i = 0; i < typeDataTable.length; i++) {
			str = typeDataStr.get(i); // 一行読み込んだ
			row = str.split("\t"); // tab 区切りで列を分けた
			for (int j = 0; j < row.length; j++) {
				typeDataTable[i][j] = Double.parseDouble(row[j]);
			}
		}
		//データチェック
//		for(double[] array: typeDataTable) {
//			for(double d: array) {
//				System.out.print(d+"\t");
//			}
//			System.out.println();
//		}
		// ここではファイルを開いた最初なので実験番号は0だとして、
		// nowExpTypeData[][] に0番目の実験結果を書き込む.0番目の実験なので行数はGENである。
		// 0行目から入れていく。
		nowExpTypeData = new double[GEN][row.length];
		// Canvas all01Canvas を作成する。データはすでにtypeDataTable に入っているので、typeデータを作成した
		// のと同じように nowExpAll01Data[][]を作ってその実験回のデータを保存しておく。
		nowExpAll01Data = new double[GEN][2];
		//
		typeDataFlag = true;
		// 横軸（世代軸）は2つのCanvasで共通なのでgraphicContext を渡して作成する。
		drawXAxis(gType);
		drawXAxis(gAll01);
		// 縦軸も共通だが、最大値が異なるため最大値も渡す。
		drawYAxis(gType, 100.0);
		drawYAxis(gAll01, 100.0);
		// 考え方を根本的に変える Mar06。
		// 最初の4列（type別個体数）を最初に書いておいて、チェックボタンで別のcanvas に
		// のこり2つを描いておき、チェックボタンで表示させるようにする。だからその処理はあとでいい。
		// だから最初の4列を順番に配列に取り出し、それを表示させる。
		// 平均値データが先に読み込まれているとすれば、nowGen に値は入っているのだから
		// すぐにChangeLestener でよいのでは。
		double[] nowTypeRecord = new double[GEN];
		double[] nowAll01Record = new double[GEN];
		// strokPolyLine に渡すための pxel 配列
		double[] yPix = new double[GEN];
		double[] xPix = new double[GEN];
		// どうもChangeLestener だけではまずそう Mar07
		// 列ごとに処理しよう
		// System.out.println("in openTypeFile() nowExp="+nowExp);
		for (int j = 0; j < 4; j++) {
			int count = 0;
			while (count < GEN) {// ただし、こうしてTable自体を更新するのはムダである。
				nowExpTypeData[count][j] = typeDataTable[nowExp * GEN + count][j];
				count++;
			}
		}
		// nowExpAll01Data は2列なのでループと別に処理
		for (int i = 0; i < GEN; i++) {
			nowExpAll01Data[i][0] = typeDataTable[nowExp * GEN+i][4];
			nowExpAll01Data[i][1] = typeDataTable[nowExp * GEN+i][5];
		}
		// データチェック
//		for(double[] array: nowExpAll01Data) {
//			for(double d : array) {
//				System.out.print(d+"\t");
//			}
//			System.out.println();
//		}

		// こんどはnowExpTypeDataテーブルから一つずつ列を取り出す。
		for (int j = 0; j < 4; j++) {
			for (int i = 0; i < GEN; i++) {
				nowTypeRecord[i] = nowExpTypeData[i][j];
			}
			// データが抜き出されたのでpixelデータを作る
			makePixelData(xPix, yPix, nowTypeRecord, gWidth, gHeight, 100.0);
			// stroke の色を設定
			gType.setStroke(typeColor[j]);
			gType.strokePolyline(xPix, yPix, GEN);
		} // end of for(最初の4列
			// All01 のグラフを描く
		for (int j = 0; j < 2; j++) {
			for (int i = 0; i < GEN; i++) {
				nowAll01Record[i] = nowExpAll01Data[i][j];
			}
			// データが抜き出されたのでpixelデータを作る
			makePixelData(xPix, yPix, nowAll01Record, gWidth, gHeight, 100.0);
			// stroke の色を設定
			gAll01.setStroke(typeColor[j]);
			gAll01.strokePolyline(xPix, yPix, GEN);
			all01Canvas.toBack();
		}
		expSpinner.valueProperty().addListener(new ChangeListener<Integer>() {
			@Override
			public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
				// check
				// System.out.println("changed is called in openType file");
				gType.clearRect(0, 0, width, height);
				gType.setFill(Color.WHITE);
				gType.fillRect(0, 0, width, height);
				gType.setStroke(Color.BLACK);
				drawXAxis(gType);
				drawYAxis(gType, 100.0);
				//All 01
				gAll01.clearRect(0, 0, width, height);
				gAll01.setFill(Color.WHITE);
				gAll01.fillRect(0, 0, width, height);
				gAll01.setStroke(Color.BLACK);
				drawXAxis(gAll01);
				drawYAxis(gAll01, 100.0);
				//
				nowExp = expSpinner.getValue();
				// typeファイルの場合は実験ごとにテーブルが更新されるので、スピナー値が変わるたびに
				// テーブルが書き換えられなければならない。
				// 列ごとに処理しよう
				for (int j = 0; j < 4; j++) {
					int count = 0;
					while (count < GEN) {// ただし、こうしてTable自体を更新するのはムダである。
						nowExpTypeData[count][j] = typeDataTable[nowExp * GEN + count][j];
						count++;
					}
				}
				// nowExpAll01Data は2列なのでループと別に処理
				for (int i = 0; i < GEN; i++) {
					nowExpAll01Data[i][0] = typeDataTable[nowExp * GEN+i][4];
					nowExpAll01Data[i][1] = typeDataTable[nowExp * GEN+i][5];
				}
				// こんどはnowExpTypeDataテーブルから一つずつ列を取り出す。
				for (int j = 0; j < 4; j++) {
					for (int i = 0; i < GEN; i++) {
						nowTypeRecord[i] = nowExpTypeData[i][j];
					}
					// データが抜き出されたのでpixelデータを作る
					makePixelData(xPix, yPix, nowTypeRecord, gWidth, gHeight, 100.0);
					// stroke の色を設定
					gType.setStroke(typeColor[j]);
					// 描画。
					gType.strokePolyline(xPix, yPix, GEN);
				}
				// end of for(最初の4列
				for (int j = 0; j < 2; j++) {
					for (int i = 0; i < GEN; i++) {
						nowAll01Record[i] = nowExpAll01Data[i][j];
						//System.out.println("exp="+nowExp+": All "+ nowAll01Record[i]);
					}
					// データが抜き出されたのでpixelデータを作る
					makePixelData(xPix, yPix, nowAll01Record, gWidth, gHeight, 100.0);
					// stroke の色を設定
					gAll01.setStroke(typeColor[j]);
					gAll01.strokePolyline(xPix, yPix, GEN);
					//all01Canvas.toBack();
				}

			} // end of changed(...
		});

	}

	//
	public void openAction(List<String> list) {
		FileChooser fc = new FileChooser();
		File file;
		fc.setTitle("open data file");
		if(dir != null) {
			fc.setInitialDirectory(new File(dir));
			file = fc.showOpenDialog(null);
		}else {
			file = fc.showOpenDialog(null);
			dir = file.getParent();
		}
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
		//

	@FXML
	void saveAveAction() {
		saveAction(aveCanvas, "Ave");
	}

	//
	@FXML
	void saveTypeAction() {
		saveAction(typeCanvas, "Type");
	}
	@FXML
	void saveAll01Action() {
		saveAction(all01Canvas,"All01");
	}

	//
	public void saveAction(Canvas c, String fileType) {

		String fileName = new String(dir + "\\" + fileType + dateName + "Exp" + nowExp + ".png");
//		FileChooser savefile = new FileChooser();
//		savefile.setTitle("Save File");
//
//		File file = savefile.showSaveDialog(null);
		File file = new File(fileName);
		// System.out.println("is file null ? " + file);
		if (file != null) {
			WritableImage writableImage = new WritableImage((int) width, (int) height);
			c.snapshot(null, writableImage);
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
		gAll01 = all01Canvas.getGraphicsContext2D();
		//
		// Canvas size の取得。すべてのCanvasで同じにしておく。のは難しいので SceneBuilder で設定。
		width = aveCanvas.getWidth();
		height = aveCanvas.getHeight();
		//
		gAve.setFill(Color.WHITE);
		gAve.fillRect(0, 0, width, height);
		gType.setFill(Color.WHITE);
		gType.fillRect(0, 0, width, height);
		gAll01.setFill(Color.WHITE);
		gAll01.fillRect(0, 0, width, height);
		// グラフエリアの上限下限・左右
		top = margin;
		bottom = (int) height - margin;
		left = margin;
		right = (int) width - margin;
		gHeight = bottom - top; // グラフ領域高さのpixel が値 0.0 - 3.0
		gWidth = right - left; // グラフ領域幅の pixel値
		// type 別グラフの色分け。本当は openTypeFiles でやったほうが良いのかもしれないが。ここでは4つに決め打ちなので
		typeColor = new Color[4];
		typeColor[0] = Color.BLUE; // あまのじゃくの色
		typeColor[1] = Color.BLACK; // お人好しの色
		typeColor[2] = Color.GREEN; // 裏切り者の色
		typeColor[3] = Color.RED; // 互恵主義者の色
		// ラベルに色のついたワクを付けるので
		border = new Border[4];
		for (int i = 0; i < 4; i++) {
			border[i] = new Border(
					new BorderStroke(typeColor[i], BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));
		}
		// ここがめんどくさい。label の配列を SceneBuilder で付けられないか？
		lb0.setBorder(border[0]);
		lb1.setBorder(border[1]);
		lb2.setBorder(border[2]);
		lb3.setBorder(border[3]);
		lb4.setBorder(border[0]);
		lb5.setBorder(border[1]);
		// 保存ファイルに付ける日時
		Calendar cal1 = Calendar.getInstance();
		int year = cal1.get(Calendar.YEAR); // 現在の年を取得
		int month = cal1.get(Calendar.MONTH); // 現在の月数-1を取得
		int day = cal1.get(Calendar.DATE);
		String[] monthArray = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jly", "Aug", "Sep", "Oct", "Nov", "Dec" }; // 月表示を見やすくするため
		dateName = new String(monthArray[month] + day + "_" + year);

	} // end of initialize
		//

	public double round(double in, int scale) {
		double after = 0.0;
		after = new BigDecimal(String.valueOf(in)).setScale(scale, RoundingMode.HALF_UP).doubleValue();
		return after;
	}
}
