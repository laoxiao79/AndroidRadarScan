package com.laoxiao.scan.androidradarscan.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.laoxiao.scan.androidradarscan.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by xiaojf on 2017/7/6.
 */

public class RadarScanView extends View {
    private static final String TAG = "RadarScanView";

    private Context mContext;
    private boolean isSearching = false;// 标识是否处于扫描状态,默认为不在扫描状态
    private Paint mPaint;// 画笔
    private Bitmap mScanBmp;// 执行扫描运动的图片
    private int mOffsetArgs = 0;// 扫描运动偏移量参数
    private Bitmap mLightPointBmp;// 标识设备的圆点-高亮
    private int mPointCount = 0;// 圆点总数
    private List<String> mPointArray = new ArrayList<String>();// 存放偏移量的map
    private Random mRandom = new Random();
    private int mWidth, mHeight;// 宽高
    int mOutWidth;// 外圆宽度(w/4/5*2=w/10)
    int mCx, mCy;// x、y轴中心点
    int mOutsideRadius, mInsideRadius;// 外、内圆半径

    private int mBitmapWidth, mBitmapHeight;//圆形图的宽高

    private float mStepSize = 0;
    private boolean flag = true;

    private float mOutSideRadius2 = 0;

    public RadarScanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public RadarScanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public RadarScanView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init(context);
    }

    /**
     * TODO<提前初始化好需要使用的对象,避免在绘制过程中多次初始化>
     *
     * @return void
     */
    private void init(Context context) {
        mPaint = new Paint();
        this.mContext = context;

        this.mLightPointBmp = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                mContext.getResources(), R.drawable.radar_light_point_ico), 24, 24, false);
    }

    /**
     * 测量视图及其内容,以确定所测量的宽度和高度(测量获取控件尺寸).
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.i(TAG, "----------onMeasure---------");
        // 获取控件区域宽高
        if (mWidth == 0 || mHeight == 0) {
            final int minimumWidth = getSuggestedMinimumWidth();
            final int minimumHeight = getSuggestedMinimumHeight();
            mWidth = resolveMeasured(widthMeasureSpec, minimumWidth) ;
            mHeight = resolveMeasured(heightMeasureSpec, minimumHeight) ;

            Log.i(TAG, "----------onMeasure---------mWidth="+mWidth+",mHeight="+mHeight);
            // 获取x/y轴中心点
            mCx = mWidth / 2;
            mCy = mHeight / 2;
            Log.i(TAG, "----------onMeasure---------mCx="+mCx+",mCy="+mCy);

            // 获取外圆宽度
            mOutWidth = mWidth / 10;
            Log.i(TAG, "----------onMeasure---------mOutWidth="+mOutWidth);

            mBitmapWidth = mWidth - 4*mOutWidth;
            mBitmapHeight = mHeight - 4*mOutWidth;
            Log.i(TAG, "----------onMeasure---------mBitmapWidth="+mBitmapWidth+",mBitmapHeight="+mBitmapHeight);

            int destWidth = mBitmapWidth - mOutWidth;
            int destHeghit = mBitmapHeight - mOutWidth;
            Log.i(TAG, "----------onMeasure---------destWidth="+destWidth+",destHeghit="+destHeghit);

            mScanBmp = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                    mContext.getResources(), R.drawable.radar_scan_img), destWidth, destHeghit, false);//mWidth - mOutWidth, mWidth - mOutWidth

            // 计算内、外半径
            mOutsideRadius = mBitmapWidth / 2 ;// 外圆的半径  //mWidth / 2
            mInsideRadius = destWidth / 3 / 2;// 内圆的半径,除最外层,其它圆的半径=层数*insideRadius  //(mWidth - mOutWidth) / 3 / 2
            Log.i(TAG, "----------onMeasure---------mOutsideRadius="+mOutsideRadius);
            Log.i(TAG, "----------onMeasure---------mInsideRadius="+mInsideRadius);

            mOutSideRadius2 = mOutsideRadius + mOutWidth;
            Log.i(TAG, "----------onMeasure---------mOutSideRadius2="+mOutSideRadius2);

            initPointPosition();
        }
    }

    /**
     * 绘制视图--从外部向内部绘制
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        Log.i(TAG, "----------onDraw---------");
        // 开始绘制最外层的圆
        mPaint.setAntiAlias(true);// 设置抗锯齿
        mPaint.setStyle(Paint.Style.FILL);// 设置填充样式
        mPaint.setColor(0xff7db5f5);// 设置画笔颜色
        // 1.开始绘制圆形
        canvas.drawCircle(mCx, mCy, mOutsideRadius, mPaint);

        // 开始绘制内3圆
        mPaint.setColor(0xff3876bc);
        canvas.drawCircle(mCx, mCy, mInsideRadius * 3, mPaint);

        // 开始绘制内2圆
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2.0f);
        mPaint.setColor(0x32ffffff);//(0xff31C9F2);
        canvas.drawCircle(mCx, mCy, mInsideRadius * 2, mPaint);

        // 开始绘制内1圆
        canvas.drawCircle(mCx, mCy, mInsideRadius * 1, mPaint);

        // 根据角度绘制对角线
        int startX, startY, endX, endY;
        double radian;

        if(flag){
            mStepSize = mStepSize + 0.5f;
            if(mStepSize == (float) (mOutWidth*2/3)){
                flag = false;
            }
        }else{
            mStepSize = mStepSize - 0.5f;
            if(mStepSize == 0.0f){
                flag = true;
            }
        }

        // 开始绘制外圆1
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(1.0f);
        mPaint.setColor(0x66ffffff);//0x66000000
        mPaint.setAlpha(40);
        canvas.drawCircle(mCx, mCy, mOutsideRadius + mOutWidth/3 + mStepSize, mPaint);

        // 开始绘制外圆2
        mPaint.setColor(0x32ffffff);//0x66000000
        mPaint.setAlpha(20);
        mOutSideRadius2 = mOutSideRadius2 + 0.5f;
        if(mOutSideRadius2 > mOutsideRadius + 2*mOutWidth){
            mOutSideRadius2 = mOutsideRadius + mOutWidth;
        }else if (mOutSideRadius2 <= mOutsideRadius + 1.4*mOutWidth && mOutSideRadius2 > mOutsideRadius + mOutWidth){
            mPaint.setAlpha(15);
        }else if (mOutSideRadius2 > mOutsideRadius + 1.4*mOutWidth && mOutSideRadius2 <= mOutsideRadius + 1.6*mOutWidth){
            mPaint.setAlpha(10);
        }else if (mOutSideRadius2 > mOutsideRadius + 1.6*mOutWidth && mOutSideRadius2 <= mOutsideRadius + 1.8*mOutWidth){
            mPaint.setAlpha(5);
        }else if (mOutSideRadius2 > mOutsideRadius + 1.8*mOutWidth && mOutSideRadius2 <= mOutsideRadius + 2*mOutWidth){
            mPaint.setAlpha(0);
        }
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(mCx, mCy, mOutSideRadius2, mPaint);

        // 3.绘制扫描扇形图
        canvas.save();// 用来保存Canvas的状态.save之后，可以调用Canvas的平移、放缩、旋转、错切、裁剪等操作.

        if (isSearching) {// 判断是否处于扫描
            canvas.rotate(mOffsetArgs, mCx, mCy);// 绘制旋转角度,参数一：角度;参数二：x中心;参数三：y中心.
            canvas.drawBitmap(mScanBmp, mCx - mScanBmp.getWidth() / 2, mCy - mScanBmp.getHeight() / 2, null);// 绘制Bitmap扫描图片效果
            mOffsetArgs += 1.5;
        } /*else {
			canvas.drawBitmap(mScanBmp, mCx - mScanBmp.getWidth() / 2, mCy
					- mScanBmp.getHeight() / 2, null);
		}*/

        // 4.开始绘制动态点
        canvas.restore();// 用来恢复Canvas之前保存的状态.防止save后对Canvas执行的操作对后续的绘制有影响.

        if (mPointCount > 0) {// 当圆点总数>0时,进入下一层判断

            Log.i(TAG, "----------onDraw---------mOffsetArgs ="+mOffsetArgs);

            int args = mOffsetArgs % 360;
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            Log.i(TAG, "----------onDraw---------mOffsetArgs ="+mOffsetArgs+",args="+args);
            // 开始绘制坐标点
            //for (int i = 0; i < mPointArray.size(); i++) {
            String[] result1 = mPointArray.get(0).split("/");
            String[] result2 = mPointArray.get(1).split("/");
            String[] result3 = mPointArray.get(2).split("/");
            String[] result4 = mPointArray.get(3).split("/");

            if(args >= 45 && args < 135) {
                if(args>=45 && args <50){
                    paint.setAlpha(0);
                }else if(args>=50 && args <55){
                    paint.setAlpha(30);
                }else if(args>=55 && args <65){
                    paint.setAlpha(60);
                }else if(args>=65 && args <75){
                    paint.setAlpha(80);
                }else if(args>=75 && args <85){
                    paint.setAlpha(100);
                }else if(args>=105 && args <115){
                    paint.setAlpha(80);
                }else if(args>=115 && args <125){
                    paint.setAlpha(60);
                }else if(args>=125 && args <130){
                    paint.setAlpha(30);
                }else if(args>=130 && args <135){
                    paint.setAlpha(0);
                }
                canvas.drawBitmap(mLightPointBmp,
                        Integer.parseInt(result1[0]),
                        Integer.parseInt(result1[1]), paint);
                Log.i(TAG, "----------onDraw---------drawBitmap1 ---");
            }else if(args >= 135 && args < 225){
                if(args>=135 && args <145){
                    paint.setAlpha(0);
                }else if(args>=145 && args <155){
                    paint.setAlpha(30);
                }else if(args>=155 && args <165){
                    paint.setAlpha(60);
                }else if(args>=165 && args <175){
                    paint.setAlpha(80);
                }else if(args>=175 && args <185){
                    paint.setAlpha(100);
                }else if(args>=205 && args <215){
                    paint.setAlpha(80);
                }else if(args>=215 && args <220){
                    paint.setAlpha(60);
                }else if(args>=220 && args <223){
                    paint.setAlpha(30);
                }else if(args>=223 && args <225){
                    paint.setAlpha(0);
                }
                canvas.drawBitmap(mLightPointBmp,
                        Integer.parseInt(result2[0]),
                        Integer.parseInt(result2[1]), paint);
                Log.i(TAG, "----------onDraw---------drawBitmap2 ---");
            }else if(args >= 225 && args < 315){
                if(args>=225 && args <235){
                    paint.setAlpha(0);
                }else if(args>=235 && args <245){
                    paint.setAlpha(30);
                }else if(args>=245 && args <255){
                    paint.setAlpha(60);
                }else if(args>=255 && args <265){
                    paint.setAlpha(80);
                }else if(args>=265 && args <285){
                    paint.setAlpha(100);
                }else if(args>=295 && args <300){
                    paint.setAlpha(80);
                }else if(args>=300 && args <305){
                    paint.setAlpha(60);
                }else if(args>=305 && args <310){
                    paint.setAlpha(30);
                }else if(args>=310 && args <315){
                    paint.setAlpha(0);
                }
                canvas.drawBitmap(mLightPointBmp,
                        Integer.parseInt(result3[0]),
                        Integer.parseInt(result3[1]), paint);
                Log.i(TAG, "----------onDraw---------drawBitmap3 ---");
            }else if((args >= 315 && args < 360 )|| (args >= 0 && args < 45)){
                if(args>=315 && args <325){
                    paint.setAlpha(0);
                }else if(args>=325 && args <335){
                    paint.setAlpha(30);
                }else if(args>=335 && args <345){
                    paint.setAlpha(60);
                }else if(args>=345 && args <355){
                    paint.setAlpha(80);
                }else if(args>=355 && args <360){
                    paint.setAlpha(100);
                }else if(args>=25 && args <30){
                    paint.setAlpha(80);
                }else if(args>=30 && args <35){
                    paint.setAlpha(60);
                }else if(args>=35 && args <40){
                    paint.setAlpha(30);
                }else if(args>=40 && args <45){
                    paint.setAlpha(0);
                }
                canvas.drawBitmap(mLightPointBmp,
                        Integer.parseInt(result4[0]),
                        Integer.parseInt(result4[1]), paint);
                Log.i(TAG, "----------onDraw---------drawBitmap4 ---");
            }
        }

        if (isSearching)
            this.invalidate();
    }

    /**
     * TODO<设置扫描状态>
     *
     * @return void
     */
    public void setSearching(boolean status) {
        this.isSearching = status;
        this.invalidate();
    }

    /**
     * TODO<新增动态点>
     *
     * @return void
     */
    public void addPoint() {
        mPointCount++;
        this.invalidate();
    }

    /**
     * TODO<解析获取控件宽高>
     *
     * @return int
     */
    private int resolveMeasured(int measureSpec, int desired) {
        int result = 0;
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.UNSPECIFIED:
                result = desired;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(specSize, desired);
                break;
            case MeasureSpec.EXACTLY:
            default:
                result = specSize;
        }
        return result;
    }

    void initPointPosition(){
        //position 1
        int mx = 2 * mOutWidth + mOutWidth / 2 + mInsideRadius * 3 + 10;
        int my = 2 * mOutWidth + mOutWidth / 2 + mInsideRadius ;
        Log.i(TAG, "----------initPointPosition---------mx1 =" + mx);
        Log.i(TAG, "----------initPointPosition---------my1 =" + my);

        mPointArray.add(mx + "/" + my);

        //position 2
        mx = 2 * mOutWidth + mOutWidth / 2 + mInsideRadius * 3 + mInsideRadius;
        my = 2 * mOutWidth + mOutWidth / 2 + mInsideRadius * 3 + mInsideRadius + 5;
        Log.i(TAG, "----------initPointPosition---------mx2 =" + mx);
        Log.i(TAG, "----------initPointPosition---------my2 =" + my);

        mPointArray.add(mx + "/" + my);

        //position 3
        mx = 2 * mOutWidth + mOutWidth / 2 + mInsideRadius * 3 - mInsideRadius;
        my = 2 * mOutWidth + mOutWidth / 2 + mInsideRadius * 3 + mInsideRadius/2;
        Log.i(TAG, "----------initPointPosition---------mx3 =" + mx);
        Log.i(TAG, "----------initPointPosition---------my3 =" + my);

        mPointArray.add(mx + "/" + my);

        //position 4
        mx = 2 * mOutWidth + mOutWidth / 2 + mInsideRadius + mInsideRadius*2/3;
        my = 2 * mOutWidth + mOutWidth / 2 + mInsideRadius + mInsideRadius*3/4;
        Log.i(TAG, "----------initPointPosition---------mx4 =" + mx);
        Log.i(TAG, "----------initPointPosition---------my4 =" + my);

        mPointArray.add(mx + "/" + my);
    }
}
