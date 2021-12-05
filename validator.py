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

def draw_venn(gt_set, sample_set, keyword):
	my_dpi=100
	plt.figure(figsize=(500/my_dpi, 500/my_dpi), dpi=my_dpi)
	g=venn2(subsets = (gt_set, sample_set),set_labels = ("Groudtruth", "outcome"))
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
	replace_dict = {}
	if keyword == "CLASSES" and is_sample:
		"""
			Whether there's "." insider of the name
		"""
		class_without_dot = set()
		 
		for c in indicators:
			if "." not in c:
				class_without_dot.add(c)

		class_with_dot = indicators - class_without_dot

		for c in class_without_dot:
			for c1 in class_with_dot:
				temp = c1
				if c1.replace(".", "").endswith(c):
					replace_dict[c] = temp
					indicators.remove(c) 

	# print(indicators)
	print(replace_dict)
	return indicators, replace_dict

def getIntersection(gt_set, sample_set):
	init_intersection = gt_set & sample_set

	"""
	to find those hand-made classes, which don't have the package names.
	the number of "." hand-made calsses have must be smaller than 2. 
	Usually they don't have ".", some special cases would be like "Configuration$Builder" and are converted into "Configuration.Builder"
	"""

	candidate_sample = sample_set - init_intersection
	new_sample_set = sample_set
	for c in candidate_sample:
		#function_name = c.split("(")[0] #Only comapre the function name
		for gt in gt_set:
			if c in gt:
			#if function_name in gt:
				init_intersection.add(gt)
				new_sample_set.remove(c)
				new_sample_set.add(gt)
				break


	return init_intersection, new_sample_set

def compareTwoFiles(gt_filepath, sample_filepath):
	gt_classes, _ = getIndicators(gt_filepath, "CLASSES", False)
	gt_apis, _ = getIndicators(gt_filepath, "API CALLS", False)
	gt_constructors, _ = getIndicators(gt_filepath, "CONSTRUCTORS", False)

	sample_classes, replace_dict = getIndicators(sample_filepath, "CLASSES", True)
	sample_apis, _ = getIndicators(sample_filepath, "API CALLS", True)
	sample_constructors, _ = getIndicators(sample_filepath, "CONSTRUCTORS", True)

	print("______________________________")

	# replace dict used in apis and constructors
	temp_sample_apis = sample_apis
	temp_sample_constructors = sample_constructors

	for origin_class_name in replace_dict:
		for api in temp_sample_apis:
			if origin_class_name in api and api.count(".") < 2:
				new_api = api.replace(origin_class_name, replace_dict[origin_class_name])
				sample_apis.remove(api)
				sample_apis.add(new_api)

		for con in temp_sample_constructors:
			if origin_class_name in con and con.count(".") < 2:
				new_con = con.replace(origin_class_name, replace_dict[origin_class_name])
				print(con)
				print(new_con)
				sample_constructors.remove(con)
				sample_constructors.add(new_con)
	
	print("sample:****")
	print(sample_apis)
	print(sample_constructors)

	print("gt:****")
	print(gt_apis)
	print(gt_constructors)

	"""
	Find the intersection between groudtruth and samples
	"""
	classes_intersection, new_class_sample = getIntersection(gt_classes, sample_classes)
	apis_intersection, new_apis_sample = getIntersection(gt_apis, sample_apis)
	constructors_intersection, new_constructor_sample = getIntersection(gt_constructors, sample_constructors)

	"""
	Find the ones in groudtruth, not in sample outcome
	"""
	classes_only_in_gt = gt_classes - classes_intersection
	apis_only_in_gt = gt_apis - apis_intersection
	constructors_only_in_gt = gt_constructors - constructors_intersection

	"""
	Find the ones in sample outcome, not in groudtruth
	"""
	classes_only_in_sample = sample_classes - classes_intersection
	apis_only_in_sample = sample_apis - apis_intersection
	constructors_only_in_sample = sample_constructors - constructors_intersection

	print(len(classes_intersection))
	print(len(apis_intersection))
	print(len(constructors_intersection))
	
	print(len(classes_only_in_gt))
	print(len(apis_only_in_gt))
	print(len(constructors_only_in_gt))

	print(len(classes_only_in_sample))
	print(len(apis_only_in_sample))
	print(len(constructors_only_in_sample))

	draw_venn(gt_classes, new_class_sample, "Class")
	draw_venn(gt_apis, new_apis_sample, "Apis")
	draw_venn(gt_constructors, new_constructor_sample, "Constructors")


if __name__ == "__main__":
	compareTwoFiles("gt.txt", "sample.txt")