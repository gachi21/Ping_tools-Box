package com.gachi21.pingtoolsbox

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.content.*
import android.widget.*
import java.net.InetAddress
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.math.abs

class MainActivity : Activity() {
 private val handler=Handler(Looper.getMainLooper())
 private val executor=Executors.newSingleThreadExecutor()
 private var running=false; private var sent=0; private var received=0
 private val times=mutableListOf<Long>()
 private lateinit var host:EditText; private lateinit var count:EditText; private lateinit var interval:EditText; private lateinit var timeout:EditText
 private lateinit var output:TextView; private lateinit var tx:TextView; private lateinit var rx:TextView; private lateinit var loss:TextView
 private lateinit var min:TextView; private lateinit var avg:TextView; private lateinit var max:TextView; private lateinit var jitter:TextView

 override fun onCreate(b:Bundle?) {
  super.onCreate(b); setContentView(R.layout.activity_main)
  host=findViewById(R.id.hostInput); count=findViewById(R.id.countInput); interval=findViewById(R.id.intervalInput); timeout=findViewById(R.id.timeoutInput)
  output=findViewById(R.id.outputText); tx=findViewById(R.id.sentText); rx=findViewById(R.id.receivedText); loss=findViewById(R.id.lossText)
  min=findViewById(R.id.minText); avg=findViewById(R.id.avgText); max=findViewById(R.id.maxText); jitter=findViewById(R.id.jitterText)
  val spinner=findViewById<Spinner>(R.id.presetSpinner)
  spinner.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,arrayOf("Presets","1.1.1.1","8.8.8.8","google.com","cloudflare.com"))
  spinner.setOnItemSelectedListener(object:AdapterView.OnItemSelectedListener{
   override fun onNothingSelected(p:AdapterView<*>?) {}
   override fun onItemSelected(p:AdapterView<*>?,v:android.view.View?,pos:Int,id:Long){if(pos>0) host.setText(p!!.getItemAtPosition(pos).toString())}
  })
  findViewById<Button>(R.id.startButton).setOnClickListener{startPing()}; findViewById<Button>(R.id.stopButton).setOnClickListener{stopPing()}
  findViewById<Button>(R.id.clearButton).setOnClickListener{clearAll()}; findViewById<Button>(R.id.copyButton).setOnClickListener{copyReport()}
  findViewById<Button>(R.id.shareButton).setOnClickListener{shareReport()}
 }
 private fun startPing(){
  stopPing(); val h=host.text.toString().trim(); if(h.isEmpty()){toast("Enter a host or IP");return}
  val maxCount=count.text.toString().toIntOrNull()?.coerceAtLeast(0)?:0
  val delay=interval.text.toString().toLongOrNull()?.coerceAtLeast(100)?:1000
  val tout=timeout.text.toString().toIntOrNull()?.coerceIn(100,10000)?:1500
  sent=0;received=0;times.clear();running=true;updateStats();append("PING $h  interval=$delay ms  timeout=$tout ms")
  executor.submit{
   while(running&&(maxCount==0||sent<maxCount)){
    val seq=sent+1;sent++;val t0=System.nanoTime()
    val ok=try{InetAddress.getByName(h).isReachable(tout)}catch(_:Exception){false}
    val ms=(System.nanoTime()-t0)/1000000
    if(ok){received++;times.add(ms)}
    handler.post{append(if(ok)"Reply from $h: seq=$seq time=${ms}ms" else "Request timeout: seq=$seq");updateStats()}
    try{Thread.sleep(delay)}catch(_:InterruptedException){break}
   }
   handler.post{running=false;append("--- ping finished ---");updateStats()}
  }
 }
 private fun stopPing(){running=false}
 private fun append(s:String){output.append("\n"+s)}
 private fun updateStats(){
  tx.text="TX $sent";rx.text="RX $received";loss.text=String.format(Locale.US,"LOSS %.1f%%",if(sent==0)0.0 else (sent-received)*100.0/sent)
  if(times.isNotEmpty()){min.text="MIN ${times.min()}ms";max.text="MAX ${times.max()}ms";avg.text=String.format(Locale.US,"AVG %.1fms",times.average());val d=times.zipWithNext{a,b->abs(b-a)};jitter.text=if(d.isEmpty())"JITTER --" else String.format(Locale.US,"JITTER %.1fms",d.average())}
 }
 private fun clearAll(){stopPing();sent=0;received=0;times.clear();output.text="PING TOOLS BOX ready.\nSelect a host and press START.";updateStats()}
 private fun report()=output.text.toString()+"\n\n"+tx.text+"  "+rx.text+"  "+loss.text+"  "+min.text+"  "+avg.text+"  "+max.text+"  "+jitter.text
 private fun copyReport(){(getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Ping Tools Box",report()));toast("Report copied")}
 private fun shareReport(){startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_TEXT,report())},"Share ping report"))}
 private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_SHORT).show()
 override fun onDestroy(){stopPing();executor.shutdownNow();super.onDestroy()}
}