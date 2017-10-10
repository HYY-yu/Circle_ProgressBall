package com.app.feng.circle_progressball;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.circleprogressball.CircleProgressBall;

public class MainActivity extends AppCompatActivity {

    private CircleProgressBall progressBall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_progressball);

        progressBall = findViewById(R.id.progressBall);


    }

    @Override
    protected void onResume() {
        super.onResume();

        TaskSomeThing taskSomeThing = new TaskSomeThing();
        taskSomeThing.execute(new Object());
        progressBall.begin();
    }

    public class TaskSomeThing extends AsyncTask<Object, Integer, Integer> {
        int temp = 0;

        @Override
        protected Integer doInBackground(Object[] params) {
            while (true) {
                try {
                    Thread.sleep(300);
                    temp++;
//                    publishProgress(temp);
                    if (temp == 50) {
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
            progressBall.indeterminatreModeFinish();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (!progressBall.getCancelFlag()) {
                progressBall.setProgress(temp);
            }
        }
    }
}
