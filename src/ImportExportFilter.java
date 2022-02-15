package com.stenway.loextensions.formats;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XNamed;
import com.sun.star.document.XExporter;
import com.sun.star.document.XFilter;
import com.sun.star.document.XImporter;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.sheet.XSpreadsheetDocument;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

public class ImportExportFilter {
	public static class _ImportExportFilter extends WeakBase implements XInitialization, XServiceInfo, XNamed, XImporter, XExporter, XFilter {
		private static final String[] serviceNames = {
			"com.stenway.loextensions.ImportExportFilter",
			"com.sun.star.document.ImportFilter",
			"com.sun.star.document.ExportFilter"
		};
		
		private static final int FILTERPROCESS_RUNS = 0;
		private static final int FILTERPROCESS_BREAK = 1;
		private static final int FILTERPROCESS_STOPPED = 2;
		
		private XComponentContext componentContext;
		private XMultiComponentFactory multiComponentFactory;
		private String internalName;
		private XSpreadsheetDocument spreadsheetDocument;
		private boolean isImport;
		private int filterProcessState;
		
		public _ImportExportFilter(XComponentContext componentContext) {
			try {
				this.componentContext = componentContext;
				this.multiComponentFactory = componentContext.getServiceManager();
				Utils.init(componentContext);
			} catch(Exception e) {
				e.printStackTrace();
			}

			internalName = "";
			spreadsheetDocument = null;
			isImport = true;
			filterProcessState = FILTERPROCESS_STOPPED;
		}
		
		public void initialize(Object[] arguments) throws com.sun.star.uno.Exception {
			if (arguments.length<1) { return; }

			PropertyValue[] propertyValues = (PropertyValue[])arguments[0];
			
			for (PropertyValue propertyValue: propertyValues) {
				if (propertyValue.Name.equals("Name")) {
					synchronized(this) {
						try {
							internalName = AnyConverter.toString(propertyValue.Value);
						} catch(IllegalArgumentException exConvert) {}
					}
				}
			}
		}
		
		public String getName() {
			synchronized(this) {
				return internalName;
			}
		}
	
		public void setName(String name) {
		}
		
		private void setDocument(XComponent component, boolean isImport) throws IllegalArgumentException {
			if (component==null) { throw new IllegalArgumentException(); }

			XServiceInfo serviceInfo = UnoRuntime.queryInterface(XServiceInfo.class, component);
			if (!serviceInfo.supportsService("com.sun.star.sheet.SpreadsheetDocument")) {
				throw new IllegalArgumentException("Wrong document type");
			}
			
			synchronized(this) {
				spreadsheetDocument = UnoRuntime.queryInterface(XSpreadsheetDocument.class, component);
				this.isImport = isImport;
			}
		}
		
		public void setTargetDocument(XComponent component) throws IllegalArgumentException {
			setDocument(component, true);
		}
		
		public void setSourceDocument(XComponent component) throws IllegalArgumentException {
			setDocument(component, false);
		}
		
		public boolean filter(PropertyValue[] propertyValues) {
			try {
				FilterStream stream;
				boolean isImportSync;
				XSpreadsheetDocument spreadsheetDocumentSync;
				String nameSync;
				synchronized(this) {
					stream = new FilterStream(isImport, propertyValues);
					isImportSync = isImport;
					spreadsheetDocumentSync = spreadsheetDocument;
					nameSync = internalName;
				}
				SpreadsheetDocument sSpreadsheetDocument = new SpreadsheetDocument(spreadsheetDocumentSync, false);
				if (isImportSync) {
					if (nameSync.equals("WsvFilter")) {
						new WsvImporter(sSpreadsheetDocument, stream).importWsv();
					} else if (nameSync.equals("TblFilter")) {
						new TblImporter(sSpreadsheetDocument, stream).importTbl();
					} else if (nameSync.equals("TblsFilter")) {
						new TblsImporter(sSpreadsheetDocument, stream).importTbls();
					} else {
						throw new RuntimeException("Unknown filter '"+nameSync+"'");
					}
				} else {
					throw new RuntimeException("Not implemented");
				}
				stream.close();
			} catch (Exception e) {
				Utils.showErrorMessage("Filter failed", e);
			}
			return true;
		}
		
		public void cancel() {
			synchronized(this) {
				if (filterProcessState == FILTERPROCESS_RUNS) {
					filterProcessState = FILTERPROCESS_BREAK;
				}
			}

			while (true) {
				synchronized(this) {
					if (filterProcessState == FILTERPROCESS_STOPPED) {
						break;
					}
				}
			}
		}
		
		public String[] getSupportedServiceNames() {
			 return serviceNames;
		}
		
		public boolean supportsService(String serviceStr) {
			return (serviceStr.equals(serviceNames[0]) ||
					serviceStr.equals(serviceNames[1]) ||
					serviceStr.equals(serviceNames[2]));
		}
		
		public String getImplementationName() {
			return _ImportExportFilter.class.getName();
		}
	}
	
	public static XSingleComponentFactory __getComponentFactory(String implementationName) {
		XSingleComponentFactory xFactory = null;

		if (implementationName.equals(_ImportExportFilter.class.getName())) {
			xFactory = Factory.createComponentFactory(_ImportExportFilter.class, _ImportExportFilter.serviceNames);
		}
		return xFactory;
	}
}