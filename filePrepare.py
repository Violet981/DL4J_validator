import os,os.path
import re

def prepareFiles(dir):
	number = 0
	for root,dirname,filenames in os.walk(dir):  
			for filename in filenames:  
				# os.path.splitext() is a tuple in the form of ('188739', '.jpg'), the index [1] can get the file format
				if os.path.splitext(filename)[1]=='.txt':
					umber += 1
					filePath = os.path.join(root, filename)
					#delete_logger(filePath)
					modify(filePath)

	print(number)

"""
modify the keywords
"""
def modify(filename):
	delete_sign = ["org.datavec.local.transforms.LocalTransformExecutor.execute(java.util.List, org.datavec.api.transform.TransformProcess)\n"
					, "org.datavec.api.transform.schema.Schema.Builder.addColumnCategorical(java.lang.String, java.lang.String...)\n"
					, "org.datavec.api.transform.TransformProcess.Builder.stringToCategorical(java.lang.String, java.lang.String...)\n"
					,"org.datavec.api.io.filters.RandomPathFilter.RandomPathFilter(java.util.Random, java.lang.String...)\n"
					,"org.datavec.api.transform.schema.Schema.Builder.addColumnDouble(java.lang.String, java.lang.Double, java.lang.Double, boolean, boolean)\n"]
	
	changeto = ["org.datavec.local.transforms.LocalTransformExecutor.execute(java.util.List<java.util.List<org.datavec.api.writable.Writable>>,org.datavec.api.transform.TransformProcess)\n",
					"org.datavec.api.transform.schema.Schema.Builder.addColumnCategorical(java.lang.String,java.util.List<java.lang.String>)\n",
					"org.datavec.api.transform.TransformProcess.Builder.stringToCategorical(java.lang.String,java.util.List<java.lang.String>)\n",
					"org.datavec.api.io.filters.RandomPathFilter.RandomPathFilter(java.util.Random, java.lang.String[])\n"
					,"org.datavec.api.transform.schema.Schema.Builder.addColumnDouble(java.lang.String, double, double, boolean, boolean)\n"
					]



	with open(filename, "r") as f:
		lines = f.readlines()
	with open(filename, "w") as f:
		for line in lines:
			if line in delete_sign:
				index = delete_sign.index(line)
				line = changeto[index]
				print(filename)
			
			f.write(line)


"""
used to delete some lines containing the logger, which is out of scope
"""
def delete_logger(filename):
	delete_sign = ["= LoggerFactory.getLogger(", "import org.slf4j.Logger", "log.info("]
	flag = False # means the file is not modified

	with open(filename, "r") as f:
		lines = f.readlines()
	with open(filename, "w") as f:
		for line in lines:
			line_flag = False
			for item in delete_sign:
				if(item in line.strip("\n")):
					line_flag = True
					flag = True
			if line_flag == False:
				f.write(line)
			else:
				line_flag = False
	if flag == True:
		print(filename)

def countAPIs(dir):
	number = 0
	classes = 0
	methods = 0
	for root,dirname,filenames in os.walk(dir):  
			for filename in filenames:  
				# os.path.splitext() is a tuple in the form of ('188739', '.jpg'), the index [1] can get the file format
				if os.path.splitext(filename)[1]=='.txt':
					number += 1
					filePath = os.path.join(root, filename)
					print(filePath)
					#delete_logger(filePath)
					gt_classes = getIndicators(filePath, "CLASSES", False)
					gt_apis = getIndicators(filePath, "API CALLS", False).union(getIndicators(filePath, "CONSTRUCTORS", False)) 
					classes += len(gt_classes)
					methods += len(gt_apis)

	print(classes/number)
	print(methods/number)



def getIndicators(filepath, keyword, is_sample):
	f = open(filepath, "r")
	text = f.read().replace('\n', "|")
	content = re.findall(keyword+'------(.*?)------END OF ' + keyword,text)
	indicators = content[0].replace(' ', '').split("|")
	del indicators[0]
	del indicators[-1]
	f.close()
	indicators = set([item for item in indicators if (("deeplearning4j" in item) or ("nd4j" in item) or ("datavec" in item))])

	return indicators	                


if __name__ == "__main__":
	# prepareFiles(r"validation/result/dl4j-examples")
	# prepareFiles(r"validation/gt/dl4j-examples")
	countAPIs(r"validation/result/dl4j-examples")