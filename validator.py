""" 6 indicators:
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

def getIndicators(filepath, keyword):
	f = open(filepath, "r")
	text = f.read().replace('\n', "|")
	content = re.findall(keyword+'------(.*?)------END OF ' + keyword,text)
	print(content)
	indicators = content[0].split("|")
	del indicators[0]
	del indicators[-1]
	f.close()
	indicators = set(indicators)
	return indicators

"""
TODO:
	still consider the hand-made classes when validating, add more tolerant

"""
def compareTwoFiles(gt_filepath, sample_filepath):
	gt_classes = getIndicators(gt_filepath, "CLASSES")
	gt_apis = getIndicators(gt_filepath, "API CALLS")
	gt_constructors = getIndicators(gt_filepath, "CONSTRUCTORS")

	sample_classes = getIndicators(sample_filepath, "CLASSES")
	sample_apis = getIndicators(sample_filepath, "API CALLS")
	sample_constructors = getIndicators(sample_filepath, "CONSTRUCTORS")

	"""
	Find the intersection between groudtruth and samples
	"""
	classes_intersection = gt_classes & sample_classes
	apis_intersection = gt_apis & sample_apis
	constructors_intersection = gt_constructors & sample_constructors

	"""
	Find the ones in groudtruth, not in sample outcome
	"""
	classes_only_in_gt = gt_classes - sample_classes
	apis_only_in_gt = gt_apis - sample_apis
	constructors_only_in_gt = gt_constructors - sample_constructors

	"""
	Find the ones in sample outcome, not in groudtruth
	"""
	classes_only_in_sample = sample_classes - gt_classes
	apis_only_in_sample = sample_apis - gt_apis
	constructors_only_in_sample = sample_constructors - gt_constructors

	print(len(classes_intersection))
	print(len(apis_intersection))
	print(len(constructors_intersection))
	
	print(len(classes_only_in_gt))
	print(len(apis_only_in_gt))
	print(len(constructors_only_in_gt))

	print(len(classes_only_in_sample))
	print(len(apis_only_in_sample))
	print(len(constructors_only_in_sample))


if __name__ == "__main__":
	compareTwoFiles("gt.txt", "sample.txt")