package cwa115.trongame;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import cwa115.trongame.Game.GameSettings;
import cwa115.trongame.Network.HttpConnector;

public class RoomActivity extends AppCompatActivity {
    HttpConnector dataServer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        dataServer = new HttpConnector(getString(R.string.dataserver_url));
        TextView roomname = (TextView)findViewById(R.id.roomname);
        roomname.setText(GameSettings.getGameName());
    }
    public void showGameActivity(View view) {
        startActivity(new Intent(this, GameActivity.class));
    }

}
