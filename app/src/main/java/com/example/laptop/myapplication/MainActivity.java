package com.example.laptop.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import br.com.caelum.stella.boleto.Banco;
import br.com.caelum.stella.boleto.Beneficiario;
import br.com.caelum.stella.boleto.Boleto;
import br.com.caelum.stella.boleto.Datas;
import br.com.caelum.stella.boleto.Endereco;
import br.com.caelum.stella.boleto.Pagador;
import br.com.caelum.stella.boleto.bancos.BancoDoBrasil;
import br.com.caelum.stella.boleto.transformer.GeradorDeBoleto;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn = (Button) findViewById(R.id.uber_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openApp(MainActivity.this,"com.ubercab", "Uber");
                //openApp(getBaseContext(), "com.projetoes.livrodereceitas");
            }
        });

        Button btn_boleto = (Button) findViewById(R.id.boleto_btn);
        btn_boleto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boleto boleto = geraBoleto();

                GeradorDeBoleto gerador = new GeradorDeBoleto(boleto);

                //Testar se assim escreve o arquivo
                //String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                //        "/PrintFiles";
                //gerador.geraPDF(file_path + "/Boletos.pdf");

                // Para gerar um boleto em PDF
                gerador.geraPDF("Boletos.pdf");


                // Para gerar um array de bytes a partir de um PDF
                byte[] bPDF = gerador.geraPDF();

                // Para gerar um boleto em PNG
                /*gerador.geraPNG("BancoDoBrasil.png");

                // Para gerar um array de bytes a partir de um PNG
                byte[] bPNG = gerador.geraPNG(); */

            }
        });

    }

    /** Open another app.
     * @param context current Context, like Activity, App, or Service
     * @param packageName the full package name of the app to open
     * @return true if likely successful, false if unsuccessful
     */
    public boolean openApp(Context context, String packageName, String appName) {

        // Use package name which we want to check
        boolean isAppInstalled = appInstalledOrNot(packageName);

        if (isAppInstalled) {
            PackageManager manager = context.getPackageManager();
            Intent i = manager.getLaunchIntentForPackage(packageName);
            if (i == null) {
                return false;
            }
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            context.startActivity(i);
            return true;
        } else {
            // Redirect to play store
            buildAlertMessageNoApplicaitonInstalled(context, appName, packageName);
        }
        return isAppInstalled;
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }

    public void buildAlertMessageNoApplicaitonInstalled(Context context, String appName, final String packageName) {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setMessage("O aplicativo " + appName + " não está instalado, deseja instalar?")
                .setCancelable(false)
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        // Redireciona para a play store
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
                        }
                    }
                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final android.app.AlertDialog alert = builder.create();
        if (alert==null){
            Toast.makeText(context,"oi?", Toast.LENGTH_LONG).show();
        } else {
            alert.show();
        }
    }

    private Boleto geraBoleto() {

        //https://github.com/caelum/caelum-stella/wiki/Gerando-boleto
        //Receber banco
        Banco banco = new BancoDoBrasil();


        Datas datas = Datas.novasDatas()
                .comDocumento(18, 5, 2017)
                .comProcessamento(18, 5, 2008)
                .comVencimento(20, 5, 2008);
        // Quem emite a boleto
        Endereco enderecoBeneficiario = Endereco.novoEndereco()
                .comLogradouro("Av das Empresas, 555")
                .comBairro("Bairro Grande")
                .comCep("01234-555")
                .comCidade("São Paulo")
                .comUf("SP");

        //Quem emite o boleto
        Beneficiario beneficiario = Beneficiario.novoBeneficiario()
                .comNomeBeneficiario("Fulano de Tal")
                .comAgencia("1824").comDigitoAgencia("4")
                .comCodigoBeneficiario("76000")
                .comDigitoCodigoBeneficiario("5")
                .comNumeroConvenio("1207113")
                .comCarteira("18")
                .comEndereco(enderecoBeneficiario)
                .comNossoNumero("9000206");

        Endereco enderecoPagador = Endereco.novoEndereco()
                .comLogradouro("Av dos testes, 111 apto 333")
                .comBairro("Bairro Teste")
                .comCep("01234-111")
                .comCidade("São Paulo")
                .comUf("SP");

        //Quem paga o boleto
        Pagador pagador = Pagador.novoPagador()
                .comNome("Fulano da Silva")
                .comDocumento("111.222.333-12")
                .comEndereco(enderecoPagador);


        Boleto boleto = Boleto.novoBoleto()
                .comBanco(banco)
                .comDatas(datas)
                .comBeneficiario(beneficiario)
                .comPagador(pagador)
                .comValorBoleto("200.00")
                .comNumeroDoDocumento("1234")
                .comInstrucoes("instrucao 1", "instrucao 2", "instrucao 3", "instrucao 4", "instrucao 5")
                .comLocaisDePagamento("local 1", "local 2");


        return boleto;


    }

}
