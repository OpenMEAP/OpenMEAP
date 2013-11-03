using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Navigation;
using Microsoft.Phone.Controls;
using Microsoft.Phone.Shell;
using OpenMEAP.Source;
using System.IO.IsolatedStorage;
using Newtonsoft.Json.Linq;
using Newtonsoft.Json;
using ICSharpCode.SharpZipLib.Zip;
using System.IO;

namespace OpenMEAP
{
    public partial class MainPage : PhoneApplicationPage
    {

        IsolatedStorageSettings appSettings = IsolatedStorageSettings.ApplicationSettings;

        // Constructor
        public MainPage()
        {
            InitializeComponent();

            if (!appSettings.Contains("URL"))
            {
                appSettings.Add("URL", Helper.URL.Main);
            }
        }

        // Navigates back in the web browser's navigation stack, not the applications.
        private void BackApplicationBar_Click(object sender, EventArgs e)
        {
            Browser.GoBack();
        }

        // Navigates forward in the web browser's navigation stack, not the applications.
        private void ForwardApplicationBar_Click(object sender, EventArgs e)
        {
            Browser.GoForward();
        }

        // Navigates to the initial "home" page.
        private void HomeMenuItem_Click(object sender, EventArgs e)
        {
            Browser.Navigate(new Uri((string)appSettings["URL"], UriKind.Relative));
        }

        private void Browser_Loaded(object sender, RoutedEventArgs e)
        {
            Browser.IsScriptEnabled = true;


            // Add your URL here
            Browser.Navigate(new Uri((string)appSettings["URL"], UriKind.Relative));
        }
 
        // Handle navigation failures.
        private void Browser_NavigationFailed(object sender, System.Windows.Navigation.NavigationFailedEventArgs e)
        {
            MessageBox.Show("Navigation to this page failed, check your internet connection");
        }

        // Handles scriptsS
        private void Browser_ScriptNotify(object sender, NotifyEventArgs e)
        {
            MessageBox.Show(e.Value);
        }

        // Handles page load
        private void Browser_LoadCompleted(object sender, NavigationEventArgs e)
        {
            if ((string)appSettings["URL"] == Helper.URL.Main)
            {
                MessageBox.Show("Application will restart once the update is complete!", "Update!", MessageBoxButton.OK);
                Config config = new Config();
                WebClient webClient = new WebClient();
                webClient.DownloadStringCompleted += webClient_DownloadStringCompleted;
                webClient.DownloadStringAsync(new Uri(config.Get("appMgmtServiceUrl") + string.Format("?action=connection-open-request&app-name={0}&app-version={1}", config.Get("appName"), config.Get("appVersion")), UriKind.Absolute));
            }
        }

        void webClient_DownloadStringCompleted(object sender, DownloadStringCompletedEventArgs e)
        {
            WebClient webClient = new WebClient();
            var json = (JObject)JsonConvert.DeserializeObject(e.Result);

            System.Uri targetUri = new System.Uri(json["connectionOpenResponse"]["update"]["updateUrl"].ToString());
            HttpWebRequest request = (HttpWebRequest)HttpWebRequest.Create(targetUri);
            //create asynchronous tast and declare callback to get data stream
            request.BeginGetResponse(new AsyncCallback(ReadWebRequestCallback), request);
        }

        private void ReadWebRequestCallback(IAsyncResult callbackResult)
        {
            using (IsolatedStorageFile myIsolatedStorage = IsolatedStorageFile.GetUserStoreForApplication())
            {
                myIsolatedStorage.CreateDirectory("containerA");
            }
            HttpWebRequest myRequest = (HttpWebRequest)callbackResult.AsyncState;
            HttpWebResponse myResponse = (HttpWebResponse)myRequest.EndGetResponse(callbackResult);
            using (StreamReader httpwebStreamReader = new StreamReader(myResponse.GetResponseStream()))
            {
                //open isolated storage to save files
                using (IsolatedStorageFile isoStore = IsolatedStorageFile.GetUserStoreForApplication())
                {
                    using (ZipInputStream s = new ZipInputStream(httpwebStreamReader.BaseStream))
                    {
                        //s.Password = "123456";//if archive is encrypted
                        ZipEntry theEntry;
                        try
                        {
                            while ((theEntry = s.GetNextEntry()) != null)
                            {
                                string directoryName = Path.GetDirectoryName(theEntry.Name);
                                string fileName = Path.GetFileName(theEntry.Name);

                                // create directory
                                if (directoryName.Length > 0)
                                {
                                    using (IsolatedStorageFile myIsolatedStorage = IsolatedStorageFile.GetUserStoreForApplication())
                                    {
                                        myIsolatedStorage.CreateDirectory("containerA/"+directoryName);
                                    }
                                }

                                if (fileName != String.Empty)
                                {
                                    //save file to isolated storage
                                    using (BinaryWriter streamWriter =
                                            new BinaryWriter(new IsolatedStorageFileStream("containerA/"+theEntry.Name,
                                                FileMode.OpenOrCreate, FileAccess.Write, FileShare.Write, isoStore)))
                                    {

                                        int size = 2048;
                                        byte[] data = new byte[2048];
                                        while (true)
                                        {
                                            size = s.Read(data, 0, data.Length);
                                            if (size > 0)
                                            {
                                                streamWriter.Write(data, 0, size);
                                            }
                                            else
                                            {
                                                break;
                                            }
                                        }
                                    }

                                }
                            }
                        }
                        catch (ZipException ze)
                        {
                            System.Diagnostics.Debug.WriteLine(ze.Message);
                        }
                    }
                }

            }
            Dispatcher.BeginInvoke(() =>
            {
                appSettings["URL"] = Helper.URL.ContainerA;
                appSettings.Save();
                Browser.Source = new Uri(Helper.URL.ContainerA, UriKind.Relative);
            });
        }

    }
}
