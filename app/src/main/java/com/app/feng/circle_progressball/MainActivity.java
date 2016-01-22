package com.app.feng.circle_progressball;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private Circle_ProgressBall progressBall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_progressball);

        progressBall = (Circle_ProgressBall) findViewById(R.id.progressBall);


        TaskSomeThing taskSomeThing = new TaskSomeThing();
        taskSomeThing.execute(new Object());

    }

    public class TaskSomeThing extends AsyncTask<Object, Integer, Integer> {
        int temp = 0;

        @Override
        protected Integer doInBackground(Object[] params) {
            //do something
            while (true) {

                try {
                    Thread.sleep(1000);
                    temp++;
                    publishProgress(temp);
                    if (temp == 100) {
                        return null;
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (!progressBall.stopFlag)
                progressBall.setProgress(temp);

        }
    }
}
