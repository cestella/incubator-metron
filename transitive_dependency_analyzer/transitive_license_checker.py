import xml.etree.ElementTree
import sys
import os
import re
good_licenses = [ ]
def get_license(pom) :
    ret = None
    with open(pom, 'r') as fp:
        contents = fp.read()
        xmlstring = re.sub(' xmlns="[^"]+"', '', contents, count=1)
        root = xml.etree.ElementTree.fromstring(xmlstring)
        for atype in root.findall('.//license'):
            d = {}
            for node in atype.getiterator():
                if node.tag == 'name':
                    ret = node.text
    return ret

def is_acceptable_license(license):
    if license is None:
        return False
    license = license.lower()
    for l in good_licenses:
        if l in license:
            return True
    return False

if __name__ == '__main__':
    repo_path = sys.argv[1]
    for artifact in sys.stdin:
        if artifact is not None and len(artifact.strip()) > 0:
            artifact = artifact.strip()
            if artifact == 'none':
                continue
            tokens = artifact.split(':')
            try:
                groupId = tokens[0]
                artifactId = tokens[1]
                version = tokens[3]
                pom_file = artifactId + "-" + version + ".pom"
                artifact_path = repo_path + "/" + groupId.replace('.','/') + "/" + artifactId + "/" + version + "/" + pom_file
                exists = os.path.exists(artifact_path)
                if exists:
                    license = get_license(artifact_path)
                    is_acceptable = is_acceptable_license(license)
                    if not(is_acceptable) :
                        if license is None:
                            license = "Unknown"
                        print artifact + "," + str(license)
            except:
                print "ERROR IN FINDING LICENSE FOR " + artifact
                raise
