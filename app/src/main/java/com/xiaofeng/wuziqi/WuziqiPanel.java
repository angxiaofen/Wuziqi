package com.xiaofeng.wuziqi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by xiaofeng on 2016/4/8.
 */
public class WuziqiPanel extends View {
    private static final int MAX_COUNT_IN_LINE = 5;
    private int MAX_LINE = 10;  //棋盘网格线的最大数
    private float mLineHeight ;  //每个格的高度
    private int mPanelHeight ; //panel的高度
    private int mPieceWidth;    //棋子的宽度

    private float mRatioPieceOfLineHeight = 3*1.0f/4;

    private Paint mPaint ;  //绘制棋盘的画笔

    private ArrayList<Point> mWhitePieces = new ArrayList<>(); // 白棋的集合
    private ArrayList<Point> mBlackPieces = new ArrayList<>(); // 黑棋的集合
    private Bitmap mWhitePiece , mBlackPiece;  // 白棋黑棋

    private boolean mIsWhite = true ; // 白棋先行或轮到白棋子落子
    private boolean mIsGameOver ;  // 游戏结束
    private boolean mIsWhiteWinner ; // 白棋胜利


    public WuziqiPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(0x44ff0000);

        init();
    }

    private void init() {
        //初始化画笔
        mPaint = new Paint();
        mPaint.setColor(0x88000000);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        // 得到白棋黑棋的图片
        mWhitePiece = BitmapFactory.decodeResource(getResources(), R.mipmap.stone_w2);
        mBlackPiece = BitmapFactory.decodeResource(getResources(),R.mipmap.stone_b1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //得到控件的高宽及mode
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = Math.min(widthSize,heightSize);  //取宽高的最小值
        //避免控件的宽高为零
        if (widthMode == MeasureSpec.UNSPECIFIED){
            width = heightSize;
        }
        if (heightMode == MeasureSpec.UNSPECIFIED){
            width = widthSize;
        }
        setMeasuredDimension(width, width);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        mPanelHeight = w;  // 得到棋盘的高度
        mLineHeight =  mPanelHeight * 1.0f / MAX_LINE; // 得到每个网格的高度

        //固定棋子的宽度
        mPieceWidth = (int) (mRatioPieceOfLineHeight * mLineHeight);
        mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece,mPieceWidth,mPieceWidth,false);
        mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece,mPieceWidth,mPieceWidth,false);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //游戏结束直接返回,终止游戏
        if (mIsGameOver){
            return false;
        }
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP){
            int x = (int) event.getX();
            int y = (int) event.getY();

            Point p = getValidPoint(x,y);

            //避免重复落子
            if (mWhitePieces.contains(p) || mBlackPieces.contains(p)){
                return false;
            }

            if (mIsWhite){
                mWhitePieces.add(p);
            }else{
                mBlackPieces.add(p);
            }
            invalidate(); // 重新绘制,调用onDraw()方法
            mIsWhite = !mIsWhite;

            return true;
        }
        return true;
    }

    //得到有效的点,规定点的坐标
    private Point getValidPoint(int x, int y) {
        return new Point((int)(x / mLineHeight) , (int)(y / mLineHeight));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBoard(canvas);  //绘制棋盘
        drawPieces(canvas); //绘制棋子

        checkGameOver();
    }

    /*
    * 检查游戏是否结束
    * */
    private void checkGameOver() {
        boolean isWhiteWin = checkFiveInLine(mWhitePieces);
        boolean isBlackWin = checkFiveInLine(mBlackPieces);

        if (isBlackWin || isWhiteWin){
            mIsGameOver = true;
            mIsWhiteWinner = isWhiteWin;
            String text = isWhiteWin ? "白棋胜利" : "黑棋胜利";
            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkFiveInLine(ArrayList<Point> points) {
        for(Point p:points){
            int x = p.x;
            int y = p.y;

            boolean win ;

            win = checkHorizontal(x, y, points);
            if (win) return true;

            win = checkVertical(x, y, points);
            if (win) return true;

            win = checkLeftDiagonal(x, y, points);
            if (win) return true;

            win = checkRightDiagonal(x,y,points);
            if (win) return true;

        }
        return false;
    }

    /*
   * 检查右斜方是否五子连线
   * */
    private boolean checkRightDiagonal(int x, int y, ArrayList<Point> points) {
        int count = 1;
        //右下
        for (int i=1;i<MAX_COUNT_IN_LINE;i++){
            if (points.contains(new Point(x+i,y+i))){
                count++;
            }else {
                break;
            }
        }
        //右上
        for (int i=1;i<MAX_COUNT_IN_LINE;i++){
            if (points.contains(new Point(x-i,y-i))){
                count++;
            }else{
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE){
            return true;
        }
        return false;
    }

    /*
   * 检查左斜方是否五子连线
   * */
    private boolean checkLeftDiagonal(int x, int y, ArrayList<Point> points) {
        int count = 1;
        //左下
        for (int i=1;i<MAX_COUNT_IN_LINE;i++){
            if (points.contains(new Point(x-i,y+i))){
                count++;
            }else {
                break;
            }
        }
        //左上
        for (int i=1;i<MAX_COUNT_IN_LINE;i++){
            if (points.contains(new Point(x+i,y-i))){
                count++;
            }else{
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE){
            return true;
        }
        return false;
    }

    /*
   * 检查横向是否五子连线
   * */
    private boolean checkHorizontal(int x, int y, ArrayList<Point> points) {
        int count = 1;
        //向右
        for (int i = 1;i<MAX_COUNT_IN_LINE;i++)
        {
            if (points.contains(new Point(x+i,y))){
                count++;
            }else{
                break;
            }
        }
        //向左
        for (int i = 1;i<MAX_COUNT_IN_LINE;i++){
            if (points.contains(new Point(x-i,y))){
                count++;
            }else{
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE){
            return true;
        }
        return false;
    }

    /*
    * 检查竖向是否五子连线
    * */
    private boolean checkVertical(int x, int y, ArrayList<Point> points) {
        int count = 1;
        //向下
        for (int i = 1;i<MAX_COUNT_IN_LINE;i++)
        {
            if (points.contains(new Point(x,y+i))){
                count++;
            }else{
                break;
            }
        }
        //向上
        for (int i = 1;i<MAX_COUNT_IN_LINE;i++){
            if (points.contains(new Point(x,y-i))){
                count++;
            }else{
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE){
            return true;
        }
        return false;
    }

    /*
    * 绘制棋子
    * */
    private void drawPieces(Canvas canvas) {
        //绘制白棋
        for (int i=0,n=mWhitePieces.size() ; i<n ; i++){
            Point p = mWhitePieces.get(i);
            canvas.drawBitmap(mWhitePiece,(p.x + 0.5f) * mLineHeight - mPieceWidth/2,
                    (p.y + 0.5f)*mLineHeight - mPieceWidth/2,null);
        }

        //绘制黑棋
        for (int i=0,n=mBlackPieces.size() ; i<n ; i++){
            Point p = mBlackPieces.get(i);
            canvas.drawBitmap(mBlackPiece,(p.x + 0.5f) * mLineHeight - mPieceWidth/2,
                    (p.y + 0.5f)*mLineHeight - mPieceWidth/2,null);
        }

    }

    /*
    * 绘制棋盘
    * */
    private void drawBoard(Canvas canvas) {
        //绘制横线
        for (int i=0;i<MAX_LINE;i++){
            canvas.drawLine(mLineHeight/2,mLineHeight/2+mLineHeight*i,mPanelHeight-mLineHeight/2,
                    mLineHeight/2+mLineHeight*i,mPaint);
        }
        //绘制竖线
        for (int i=0;i<MAX_LINE;i++){
            canvas.drawLine(mLineHeight/2+mLineHeight*i,mLineHeight/2,mLineHeight/2+mLineHeight*i,
                    mPanelHeight-mLineHeight/2,mPaint);
        }
    }
}
