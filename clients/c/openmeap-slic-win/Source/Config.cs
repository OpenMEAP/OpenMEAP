using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;

namespace OpenMEAP.Source
{
    class Config
    {
        XDocument xDoc;
        public Config()
        {
            xDoc = XDocument.Load("Supporting Files/slic-config.xml");
        }

        public string Get(string name)
        {
            return xDoc.Descendants(name).Single().Value;
        }
    }
}
