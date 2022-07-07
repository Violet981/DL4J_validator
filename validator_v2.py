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
#conding=utf8  
import os 


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
	plt.show()


def getIndicators(filepath, keyword, is_sample):
	f = open(filepath, "r")
	text = f.read().replace('\n', "|")
	content = re.findall(keyword+'------(.*?)------END OF ' + keyword,text)
	indicators = content[0].replace(' ', '').split("|")
	del indicators[0]
	del indicators[-1]
	f.close()
	indicators = set(indicators)
	return indicators

def getIntersection(gt_set, sample_set):
	init_intersection = gt_set.intersection(sample_set)
	loose_intersection = init_intersection

	candidate_sample = sample_set - init_intersection
	for c in candidate_sample:
		function_name = c.split("(")[0] #Only comapre the function name
		for gt in gt_set:
			#if c in gt:
			if function_name in gt:
				loose_intersection.add(gt)
				break


	return init_intersection, loose_intersection

def compareTwoFiles(gt_filepath, sample_filepath):
	gt_classes = getIndicators(gt_filepath, "CLASSES", False)
	gt_apis = getIndicators(gt_filepath, "API CALLS", False)
	gt_constructors = getIndicators(gt_filepath, "CONSTRUCTORS", False)

	sample_classes = getIndicators(sample_filepath, "CLASSES", True)
	sample_apis = getIndicators(sample_filepath, "API CALLS", True)
	sample_constructors = getIndicators(sample_filepath, "CONSTRUCTORS", True)

	print("______________________________")
	print(os.path.splitext(gt_filepath)[0])

	# draw the venn for each sample
	draw_venn(gt_classes,sample_classes, "Class")
	draw_venn(gt_apis,sample_apis, "API")
	draw_venn(gt_constructors,sample_constructors, "Constructor")


	"""
	Find the intersection between groudtruth and samples
	"""
	classes_intersection, classes_loose_intersection = getIntersection(gt_classes, sample_classes)
	apis_intersection, apis_loose_intersection = getIntersection(gt_apis, sample_apis)
	constructors_intersection, constructors_loose_intersection = getIntersection(gt_constructors, sample_constructors)

	print("sample_classes len: "+ str(len(sample_classes)))
	print("sample_apis len: "+ str(len(sample_apis)))
	print("sample_constructors len: "+ str(len(sample_constructors)))

	# """
	# Find the ones in groudtruth, not in sample outcome
	# """
	classes_only_in_gt = gt_classes - sample_classes
	apis_only_in_gt = gt_apis - sample_apis
	constructors_only_in_gt = gt_constructors - sample_constructors

	print(classes_only_in_gt)
	print(apis_only_in_gt)
	print(constructors_only_in_gt)

	recall_class = len(classes_intersection) / len(gt_classes)
	recall_api = len(apis_intersection) / len(gt_apis)
	recall_constructors = len(constructors_intersection) / len(gt_constructors)

	recall = []
	recall.append(recall_class)
	recall.append(recall_api)
	recall.append(recall_constructors)

	precision_class = len(classes_intersection) / len(sample_classes)
	precision_api = len(apis_intersection) / len(sample_apis)
	precision_constructors = len(constructors_intersection) / len(sample_constructors)

	precision = []
	precision.append(precision_class)
	precision.append(precision_api)
	precision.append(precision_constructors)

	print(recall)
	print(precision)

	return recall



if __name__ == "__main__":
	# """
	# 	for validation
	# """
	# gt = os.walk(r"validation/gt/datapipeline-examples")
	# sample = os.walk(r"validation/sample/datapipeline-examples")

	"""
		for testing
	"""
	gt = os.walk(r"validation/gt/datapipeline-examples")
	sample = os.walk(r"validation/sample/datapipeline-examples")
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
	
	for gt, sample in zip(gt_list, sample_list):
		compareTwoFiles(gt, sample)
		pass

	
		

