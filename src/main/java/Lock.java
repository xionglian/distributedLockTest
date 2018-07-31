import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.RetryOneTime;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Auther: xionglian
 * @Date: 2018/7/31 14:42
 * @Description:
 */
public class Lock {
    private static ExecutorService service;

    static final CuratorFramework curator;
    //A re-entrant mutex
    static final InterProcessMutex zkMutex;
    static {
//        curator = CuratorFrameworkFactory.newClient("server1:2182,server2:2181,server3:2181", new RetryOneTime(2000));
        curator = CuratorFrameworkFactory.newClient("qq.yuxingyue.cn:2181", new RetryOneTime(2000));
        curator.start();
        zkMutex = new InterProcessMutex(curator,"/mutex");
    }


    public static void count(int threadNum,final int workers) throws Exception{
        final CountDownLatch latch = new CountDownLatch(threadNum);
        service = Executors.newFixedThreadPool(threadNum);
        long start=System.currentTimeMillis();
        for (int i = 0; i < threadNum; ++i) {
            service.execute(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < workers; ++i) {
                        try {
                            zkMutex.acquire();
                           // System.out.println(Thread.currentThread()+" "+i);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                zkMutex.release();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    latch.countDown();
                }
            });
        }
        service.shutdown();
        latch.await();
        long end=System.currentTimeMillis();
        System.out.println("Thread Num:"+threadNum+" workers per Thread:"+workers+" cost time:"+(end-start) +" avg "+ (threadNum*workers)*1000/(end-start));
    }

    public static void main(String[] args) throws Exception {
        Lock.count(1, 10);
        Lock.count(10, 10);
        Lock.count(20, 10);
        Lock.count(30, 10);
        Lock.count(40, 10);
        Lock.count(50, 10);
        Lock.count(60, 10);
        Lock.count(70, 10);
        Lock.count(80, 10);
        Lock.count(90, 10);
        Lock.count(100, 10);
    }
}
