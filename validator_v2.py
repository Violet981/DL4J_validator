""" 9 indicators:
		Groundtruth:
			# of classes in Groudtruth not in Samples;
			# of apis in Groudtruth not in Samples;
			# of constructors in Groudtruth not in Samples;
		Sample:
			# of classes in Sample not in Groudtruth;
			# of apis in Sample not in Groudtruth;
			# of constructors in Sample not in Groudtruth;
"""
import re
import matplotlib.pyplot as plt
from matplotlib_venn import venn2,venn2_circles
from matplotlib.cbook import flatten
#conding=utf8  
import os 
import numpy

def draw_venn(gt_set, sample_set, keyword):
	if gt_set == None and sample_set == None:
		return
	my_dpi=100
	plt.figure(figsize=(500/my_dpi, 500/my_dpi), dpi=my_dpi)
	g=venn2(subsets = (gt_set, sample_set),set_labels = ("Groudtruth", "Outcome"))

	# to print the miss ones
	#g.get_label_by_id('100').set_text('\n'.join(map(str,gt_set-sample_set)))

	for text in g.set_labels:
		if text == None:
			break
		text.set_fontsize(17)
	for text in g.subset_labels:
		if text == None:
			break
		text.set_fontsize(20)

	plt.title("GT and Outcome Compare in " + keyword)
	return plt


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


def compareTwoFiles(gt_filepath, sample_filepath):
	gt_classes = getIndicators(gt_filepath, "CLASSES", False)
	gt_apis = getIndicators(gt_filepath, "API CALLS", False).union(getIndicators(gt_filepath, "CONSTRUCTORS", False)) 

	sample_classes = getIndicators(sample_filepath, "CLASSES", True)
	sample_apis = getIndicators(sample_filepath, "API CALLS", True).union(getIndicators(sample_filepath, "CONSTRUCTORS", True))

	print("______________________________")
	print(os.path.splitext(gt_filepath)[0])

	ratios = [len(gt_classes)+ len(sample_classes), len(gt_apis)+ len(sample_apis)]
	norm = [n / sum(ratios) for n in ratios]

	figure, axis = plt.subplots(1, 2, gridspec_kw=dict(width_ratios=norm))


	# draw the venn for each sample
	class_plt = venn2(subsets = (gt_classes, sample_classes),set_labels = ("Groudtruth", "Outcome"),ax=axis[0])
	api_plt = venn2(subsets = (gt_apis, sample_apis),set_labels = ("Groudtruth", "Outcome"),ax=axis[1])

	for text in class_plt.set_labels:
		text.set_fontsize(9)

	for text in api_plt.set_labels:
		text.set_fontsize(9)

	axis[0].title.set_text("Intersection between \n Groundtruth and Result \n in Classes")
	axis[1].title.set_text("Intersection between \n Groundtruth and Result \n in Methods")
	head, tail = os.path.split(gt_filepath)
	fileName = tail.split(".")[0]
	figure.tight_layout()

	plt.tight_layout(pad=0.1, w_pad=0.3, h_pad=0.3)
	plt.savefig(head + "/fig/" + fileName+ ".png")
	plt.close()


	"""
	Find the intersection between groudtruth and samples
	"""
	classes_intersection = gt_classes.intersection(sample_classes)
	apis_intersection = gt_apis.intersection(sample_apis)

	print("sample_classes len: "+ str(len(sample_classes)))
	print("sample_apis len: "+ str(len(sample_apis)))

	# """
	# Find the ones in groudtruth, not in sample outcome
	# """
	classes_only_in_gt = gt_classes - sample_classes
	apis_only_in_gt = gt_apis - sample_apis

	print(classes_only_in_gt)
	print(apis_only_in_gt)


	recall_class = len(classes_intersection) / len(gt_classes)
	recall_api = len(apis_intersection) / len(gt_apis)


	recall = []
	recall.append(recall_class)
	recall.append(recall_api)

	precision_class = len(classes_intersection) / len(sample_classes)
	precision_api = len(apis_intersection)/ len(sample_apis)

	precision = []
	precision.append(precision_class)
	precision.append(precision_api)

	print(recall)
	print(precision)

	return recall, precision



if __name__ == "__main__":
	# """
	# 	for validation
	# """
	# gt = os.walk(r"validation/gt/datapipeline-examples")
	# sample = os.walk(r"validation/sample/datapipeline-examples")

	"""
		for testing
	"""
	gt = os.walk(r"validation/gt/dl4j-examples")
	sample = os.walk(r"validation/result/dl4j-examples")
	gt_list = []
	sample_list = []

	for path,dir_list,file_list in gt:
		for file_name in file_list:
			gt_file = os.path.join(path, file_name)
			gt_list.append(gt_file)

	for path,dir_list,file_list in sample:
		for file_name in file_list:
			sample_file = os.path.join(path, file_name)
			sample_list.append(sample_file)
	
	recall_list = []
	precision_list = []
	for gt, sample in zip(gt_list, sample_list):
		recall_temp, precision_temp = compareTwoFiles(gt, sample)
		recall_list.append(recall_temp)
		precision_list.append(precision_temp)

	print("___________")
	print(recall_list)
	print(precision_list)

	class_recall = [item[0] for item in recall_list]
	api_recall = [item[1] for item in recall_list]

	recall_data = [class_recall,api_recall]

	class_precision = [item[0] for item in precision_list]
	api_precision = [item[1] for item in precision_list]

	precision_data = [class_precision,api_precision]

	print("___________")
	print(recall_data)
	print(precision_data)
	print(type(class_precision))
	print(type(precision_data))

	box_fig, ax = plt.subplots(2,2,figsize = (7,7))
	 
	# Creating plot
	dp1 = ax[0][0].boxplot(recall_data,showmeans=True, meanline=True, medianprops={'linewidth': 0})
	dp2 = ax[0][1].boxplot(precision_data,showmeans=True, meanline=True, medianprops={'linewidth': 0})

	ax[0][0].set_xticklabels(['Class', 'Method'])
	ax[0][1].set_xticklabels(['Class', 'Method'])

	ax[0][0].legend([dp1['means'][0]], ['mean'])
	ax[0][1].legend([dp2['means'][0]], ['mean'])

	for line in dp1['means']:
		# get position data for median line
		x, y = line.get_xydata()[0] # top of median line
		# overlay median value
		ax[0][0].text(x, y, '%.3f' % y, horizontalalignment='center') # draw above, centered

	for line in dp2['means']:
		# get position data for median line
		x, y = line.get_xydata()[0] # top of median line
		# overlay median value
		ax[0][1].text(x, y, '%.3f' % y,horizontalalignment='center') # draw above, centered
	
	ax[1][0].hist(recall_data, color = ['#E69F00', '#56B4E9'], label =['Class', 'API'])
	ax[1][1].hist(precision_data, color = ['#E69F00', '#56B4E9'], label =['Class', 'API'])

	ax[1][0].set_ylim([0, 20])
	ax[1][1].set_ylim([0, 20])
	ax[1][0].legend()
	ax[1][1].legend()

	
	ax[0][0].set_title('Box Plot for Recall')
	ax[1][0].set_title('Histogram for Recall')
	ax[0][1].set_title('Box Plot for Precision')
	ax[1][1].set_title('Histogram for Precision')


	plt.subplots_adjust(hspace=0.2, wspace=0.2)
	plt.savefig("validation/gt/dl4j-examples/fig/validation.png")
	plt.show()

	plt.close()

		

	
		

