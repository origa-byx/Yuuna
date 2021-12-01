/**
  一些没用的东西（雾）！
           { VelocityTracker } RecyclerView处理滑动惯性的类
              VelocityTracker mVelocityTracker = VelocityTracker.obtain();//默认构造
                   关于  mVelocityTracker.addMovement(e); // => {将用户的运动添加到跟踪器。
                                                               您应该为初始 MotionEvent.ACTION_DOWN、
                                                               您收到的以下 MotionEvent.ACTION_MOVE
                                                               事件以及最终的 MotionEvent.ACTION_UP 调用此方法。
                                                               但是，您可以为您想要的任何事件调用它。}
              添加事件到跟踪器，***----> 在源码中并没有直接 add 原本的入参，而是copy了一份add后释放内存（add 内部实现是 native 方法）
              参考：
                    VelocityTracker mVelocityTracker;
                    if (mVelocityTracker == null) { mVelocityTracker = VelocityTracker.obtain(); }
                    MotionEvent copy_e = MotionEvent.obtain(e);//拷贝
                    mVelocityTracker.addMovement(copy_e);
                    copy_e.recycle();


                    Action_Up事件中获取速度：(其他时候解析应该也无所谓，猜想native存储实现其实就是个数组，下标大小就是时间轴方向)
                        //解析跟踪到的所有事件点
                        mVelocityTracker.computeCurrentVelocity(1000, 8000);
                             ===>{ args[0] -> 单位 – 您希望速度采用的单位。值为 1 提供每毫秒像素，1000 提供每秒像素等。 }
                             ===>{ args[1] -> maxVelocity – 可以通过此方法计算的最大速度。 该值必须在与单位参数相同的单位中声明。 该值必须为正。 }

                        //获取速度
                                {检索最后计算的 X 速度。 在调用此函数之前，您必须先调用 computeCurrentVelocity(int)。
                                      参数：mScrollPointerIdid – 返回哪个指针的速度。
                                      返回：先前计算的 X / Y 速度。}
                        mVelocityTracker.getXVelocity(mScrollPointerId); - float
                        mVelocityTracker.getYVelocity(mScrollPointerId); - float

                        //重置
                        mVelocityTracker.clear()
 */