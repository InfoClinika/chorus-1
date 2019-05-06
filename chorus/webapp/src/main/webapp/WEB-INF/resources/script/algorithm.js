"use strict";

(function () {

    angular.module("support-page")
        .factory("Algorithms", function () {
            var algorithms = [
                {
                    title: "General",
                    content: "The algorithm described here is intended for a specific set of hardware instruments (those with FTMS filter)\
                Basically there are two big steps in dMS data processing:\
                <ul>\
                    <li>feature and isotope groups detection.</li>\
                    <li>isotope groups matching across many files.</li>\
                </ul>"
                },
                {
                    title: "Data Overview",
                    content: "Raw data input to the algorithm comes as 2 dimensional images. Horizontal axis corresponds to retention time points, \
                and vertical axis - mass over charge. These images may look like this:\
                <div></br>\
                        <a href=\"../img/algorithm/raw.png\" target=\"_blank\">\
                        <img src=\"../img/algorithm/raw.png\"/>\
                    </a>\
                </div>\
                </br>\
                It is typical to get single data file with m/z range from 300 to 2000 and with 4000 scans. Taking into account spectral resolution we get approximately 250000 m/z points. \
                Total memory required to hold this data is almost 4 GB, which makes it impractical to manipulate all the data at once.\
                <br>\
                So full image is split along m/z dimension into parts which we will call \'chunks\' in this documentation. One chunk has 5000 m/z points. \
                Following the example data above, we get 50 chunks. Each chunk can be treated as an image with width equals to 4000 and height equals to 5000."
                },
                {
                    title: "Features and Isotope Groups Detection",
                    content: "Molecules entering the instrument leave a trace on the images as shown on the image below. We will call this a two dimensional feature: \
                at each retention time a molecule forms a spectral peak on the records. The goal of data processing algorithm is to group traces left by the same\
                molecule species into features and then to group features belonging to isotopes of the same chemical formula.\
                <div></br>\
                        <a href=\"../img/algorithm/feature2d.png\" target=\"_blank\">\
                        <img src=\"../img/algorithm/feature2d.png\"/>\
                    </a>\
                </div>\
                Feature detection is the first phase and isotope groups detection is the second phase. Feature detection itself has two steps:\
                <ul>\
                    <li>spectral peak detection.</li>\
                    <li>2D features assembly from spectral peaks.</li>\
                </ul>"
                },
                {
                    title: "Spectral peak detection",
                    content: "The shape of individual spectral peak is very close to gaussian. \
                Due to specific data processing within the hardware almost all spectral peaks start and end with exact zero in the data, see this picture:\
                <div></br>\
                      <a href=\"../img/algorithm/scan.png\" target=\"_blank\">\
                        <img src=\"../img/algorithm/scan.png\"/>\
                      </a>\
                </div>\
                </br>\
                Spectral peaks are detected independently for each m/z chunk. After processing we collect all detected spectral peaks in lists - one list for each chunk. \
                The lists are filtered then - we exclude spectral peaks that has non gaussian shape. This is based on Pearson correlation threshold (default value is 0.6)."
                },
                {
                    title: "2D feature assembly",
                    content: "Features are assembled from spectral peaks that has close enough m/z maximum position. The process runs independently for all m/z chunks. The procedure is as follows:\
                <ul>\
                    <li>all spectral peaks are sorted on intensity (the value at spectral peak\'s maximum).</li>\
                    <li>assembly is started from most intense spectral peak.</li>\
                    <li>we collect spectral peaks to the left and to the right of starting spectral peak: collection stops when for some number of time point\
                        we cannot find any spectral peak whose m/z maximum is close enough to the one of starting spectral peak. \
                        Exact acceptable deviation depends on observed noise in m/z maximums in adjacent spectral peaks.\
                    </li>\
                    <li>assembled features are checked to contain some minimum number of adjacent spectral peaks.</li>\
                </ul>"
                },
                {
                    title: "Isotope groups detection",
                    content: "At the upper level we select one feature (which does not belong to any isotope group yet),\
                 go over all possible charges and select the one which has the best fit to average theoretical isotopic distribution. For each charge to be tested the procedure is as follows:\
                    <ul>\
                        <li>calculate distance between isotopic features in m/z index units.</li>\
                        <li>calculate theoretic distribution given mass.</li>\
                        <li>calculate maximum possible monoisotopic feature position error given theoretical distribution. \
                        Errors may arise for massive peptides when monoisotopic features fall below instrument detection level and isotope group envelope has several topmost features with similar intensity. \
                        The calculated quantity is estimated maximum error in a number of monoisotopic feature.\
                        We use hardcoded threshold of 0.7 from top feature and calculate how many features have intensity greater than the threshold.\
                        </li>\
                        <li>collect features using the step up and down from the starting feature. \
                        Collection is done iteratively as follows: start from the original feature and add new feature if its \
                        spectral peaks contain the point with coordinates x = starting_feature.x, y = starting_feature.y + y_step * i, where i goes from 1 to maximum number of features (we get it from theoretical distribution).\
                         Iterative procedure stops if we cannot find features near the search point. There are cases when search point goes beyond current m/z chunk. In these cases we look up data from adjacent chunk. \
                         When a feature from adjacent chunk is added to the isotope group we need to create its copy (and also copies of all its spectral peaks), as m/z chunk processing must be independent.\
                        </li>\
                        <li>check that we have at least 2 features found.</li>\
                        <li>fit theoretical distribution using linear regression (no bias).</li>\
                        <li>filter outliers: iteratively filter out features from the beginning and the end of sequence is their intensity differs much from predicted theoretical value. \
                        Maximum allowed relative intensity difference is 1.5.</li>\
                        <li>check if after filtering we still have at least 2 features.</li>\
                        <li>detect best shift between theoretical and observed intensity distributions. This helps when monoisotopic feature is not present. \
                        The detection procedure searches a few shifts, based on maximum possible shift calculated previously.</li>\
                        <li>if monoisotopic feature is missing - add fake feature with intensity set to zero.</li>\
                        <li>filter outliers one more time. The procedure is the same - features with big relative intensity differences from theoretical distributions are excluded</li>\
                        <li>check feature count to be at least 2.</li>\
                        <li>calculate Chi-squared statistics and query cumulative distribution. The number of degrees of freedom is feature count - 1.</li>\
                        <li>calculated p-value is used as the isotope group score. This score is assigned to each feature in the isotope group.</li>\
                        <li>check which features among selected ones already assigned isotope group score less than our calculated score. Require that features we can take are continuous in m/z. \
                        That is, for example, if feature #3 already has higher score then we disregard all following features (#4, #5, etc).</li>\
                        <li>if all checks are successful create new isotope group and include features that pass all filtering stages. \
                        if some feature from the final list already belongs to some isotope group - disassemble that group. \
                        We maintain two way linking between features and isotope group: all features have reference to their isotope group and that isotope group has list of its features.</li>\
                    </ul>\
                    <br>\
                    The procedure described is performed independently for each m/z chunk. After isotope grouping is done for all chunks we need to merge isotope groups to get a single list. \
                    The main difficulty here arises for isotope groups that cross m/z chunks borders. We limit isotope group m/z range to be at most in two adjacent chunks. The merging procedure is as follows:\
                    <br>\
                    <ul>\
                        <li>collect list of features whose isotope group was assembled in different chunk. These features are clones and thus are duplicated.</li>\
                        <li>search for original features for all duplicates and substitute clones with originals. \
                        In some cases original features may have different isotope group assigned to them. To resolve ambiguity we use isotope group score to select the best assembly.</li>\
                        <li>filter out isotope groups which were left with too few features after merge.</li>\
                    </ul>\
                    <br>\
                    An example of colored 2D image for final isotope grouping is shown below. All features belonging to the same isotope group have the same color.\
                    <br>\
                        <div></br>\
                        <a href=\"../img/algorithm/isotope_grouping.png\" target=\"_blank\">\
                        <img src=\"../img/algorithm/isotope_grouping.png\"/>\
                        </a>\
                    </div>\
                    </br>\
                    The last step in image processing is creation of clean image to use for visualizations on web UI. \
                    Cleaned data is produced from raw data but includes only features from detected isotope groups."
                },
                {
                    title: "Isotope Groups Matching Across Files",
                    content: "Usually it is required to match molecules found in one data file with those found in other files.\
                     As the molecules are somehow sorted in retention time we need to search for a specific molecule only in relatively small time ranges in other files. \
                    To simplify the search we first perform retention time alignment between all files. This images shows aligned slices for 9 files, where we matched the same molecule:\
                 <br>\
                 <div></br>\
                      <a href=\"../img/algorithm/aligned_ig.png\" target=\"_blank\">\
                      <img src=\"../img/algorithm/aligned_ig.png\"/>\
                      </a>\
                 </div>\
                 </br>\
                    <span class=\"center-text bold\">Aligning retention times across files</span>\
                <br>\
                To align files we find alignment curves - dependency of retention time (RT) in one file on retention file in another file. \
                The procedure takes two files at once. First we find matching isotope groups in these two files: for each isotope group in one file we find best match in another file. \
                The matching procedure uses RT and m/z profiles correlation for this:\
                <ul>\
                    <li>Take source isotope group and find all groups in another file which has at least one feature with very close m/z to any feature in the source group. \
                    In practice we require that m/z indexes difference be less than or equal to 1. Collected isotope groups from another file are stored in the list.</li>\
                    <li>For each collected isotope group from the list calculate matching score.</li>\
                    <li>Drop all groups with low scores. Default threshold is 0.4 The score calculation algorithm uses Pearson correlation between concatenated chromatograms for all features as follows:</li>\
                    <li>Calculate RT limits for source and target isotope groups. RT range should start from minimum RT among all features and end at maximum RT among all features.</li>\
                    <li>Align the two isotope groups so that their chromatographic shape maximums match.</li>\
                    <li>Sort all features on m/z.</li>\
                    <li>Start from lowest m/z and compose two concatenated arrays from feature\'s chromatograms - one array for isotope group.</li>\
                    <li>Calculate Pearson correlation between two arrays. We calculate correlation several times for different shifts on RT axis. \
                    Currently we use +/- 3 time points as shift limits. The best correlation is used as a score.</li>\
                </ul>\
                <br>\
                Note that if a feature from one isotope group does not correspond (on m/z axis) to any feature in the other isotope group then one array will contain valid feature\'s chromatogram data, while the second array will contain zeros for this feature. \
                To speed up the search some of the data is indexed. \
                As the matching procedure is performed for all isotope groups in each data file we will have a graph of matching between isotope groups in all files.\
                <br>\
                When all matching is done for two files we can start to detect average RT alignment. For this we build a density map for all matches. \
                The map is 2D array of bins, horizontal axis is RT position of source isotope groups and vertical axis is the shift between RT positions in source and target files.\
                Each bin contains number of isotope group matches. Sample density map is shown in this image:\
                <br>\
                <div></br>\
                    <a href=\"../img/algorithm/align_density.jpg\" target=\"_blank\">\
                    <img src=\"../img/algorithm/align_density.jpg\"/>\
                    </a>\
                </div>\
                </br>\
                Color brightness corresponds to number of isotope group matches for the bin. Having built density array we extract most probable shifts for each time point. \
                Also we extract shift limits going up and down from max probability point till probability drops as some factor (default is 0.6).\
                <br>\
                As the described algorithm relies heavily on accurate density, we need a lot of matches to build reliable align curve.\
                In cases we dont have enough data points we use a fall back algorithm based on nearest neighbor search.\
                <br>\
                    <span class=\"center-text bold\">Nearest neighbor alignment algorithm</span>\
                <br>\
                The algorithm is based on searching for N nearest neighbors for each target RT. \
                Then shifts reported by these N points are averaged to get most probable shift. \
                The input to the algorithm as was for the main algorithm is a set of shift points. \
                Each shift point contains detected isotope group match RT position and shift together with match score (which is a Pearson correlation coefficient).\
                <br>\
                Detailed algorithm is as follows:\
                <br>\
                <ul>\
                    <li>Build RT grid of count equals to number of available shift data points.</li>\
                    <li>For each RT grid point find 20 nearest (in RT) points, but don\'t take those with score less then 0.7</li>\
                    <li>Use weighted average to get most probable shift. Weight for a data point is calculated using this formula:\
                    calcDistance(pj, pi) * pj.logIntensity * pj.corr * pj.corr,\
                    where pi - source data point and pj is a data point from nearest neighbors,\
                    logIntensity - logarithm of isotope group intensity,\
                    corr - isotope match pearson correlation, calcDistance - L1 distance between pi and pj in the space of correlation and RT (in indexes)</li>\
                    <li>upper and lower limits are fixed to be 20 RT points from calculated most probable shift</li>\
                </ul>\
                <br>\
                    <span class=\"center-text bold\">Isotope group merging</span>\
                <br>\
                Now when we have calculated alignment curves for all files we can start merging isotope groups into \"master\" groups. \
                The algorithm is based on creating a match graph between all isotope groups in all files. \
                The graph is needed to resolve ambiguity. In the worst case memory footprint of the graph is O(N2), where N is the number of isotope groups. \
                To make the algorithm more scalable we split list of isotope groups in equal parts sorted on m/z. \
                The first step is to build the matching graph, which is the same as for alignment procedure except we dont match groups if they are too far apart in RT dimension taking into account values of alignment shifts.\
                The we traverse the graph in several passes to disambiguate and assemble \"master\" isotope group table.\
                <br>\
                    <span class=\"center-text bold\">Pass 1. Detect confirmed matches</span>\
                <br>\
                The goal of this pass is to collect matches with high correlation and without conflicts.\
                That is for each isotope group in each file we collect a list of \"confirmed\" matches - at most one isotope group for each of other files. A match regarded \"confirmed\" if:\
                <ul>\
                    <li>it is a two way match: both matched isotope groups has themselves as best matches for the corresponding files.</li>\
                    <li>matching correlation is high (default is 0.7)</li>\
                    <li>matched isotope groups has the same charge and monoisotopic m/z</li>\
                </ul>\
                <br>\
                    <span class=\"center-text bold\">Pass 2. Collect confirmed isotope groups</span>\
                <br>\
                Now that we have a graph of connections between isotope groups across all files we need to resolve conflicts. \
                Conflict arises when isotope group \"A\" in file 1 matches some isotope group \"B\" in file 2 and isotope group \"B\" has a match - \"C\" in file 3, but \"C\" matches isotope group \"D\" from file 1 and not \"A\".\
                Pass 2 algorithm resolves conflicts by choosing more intense isotope groups among conflicting ones. \
                So in the example above the algorithm will choose between isotope groups \"A\" and \"D\" for file 1 taking into account their intensities.\
                All conflicting isotope group other than the selected one will be disconnected from the graph.\
                <br>\
                    <span class=\"center-text bold\">Pass 3. Find unmatched groups and correct</span>\
                <br>\
                This pass is eliminating conflicting matches in the graph. \
                For instance if for file 1 isotope group \"A\" is matched to isotope group \"B\" in file 2 and isotope group \"B\" itself is matched to isotope group \"C\" in file 3. But isotope group \"C\" has different charge.\
                In cases like this we use majority voting to select one charge and monoisotopic m/z for all linked isotope groups.\
                <br>\
                After this pass we assemble \"master\" isotope group table, where each entity has averaged parameters (rt, mz, mass, ...) and links to graph entry point.\
                <br>\
                    <span class=\"center-text bold\">Pass 4. Merge master isotope groups</span>\
                <br>\
                This is an iterative pass. During each iteration we traverse master list and merge some master groups into one. Iterations stop when there are no more master groups to merge."
                }
            ];

            return {
                algorithm: algorithms
            };

        });
})();
