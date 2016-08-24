import sets
import sys

def read_component(i):
    with open(i, 'r') as fp:
        component_lines = fp.readlines()
        ret = []
        for line in component_lines:
            if len(line) > 0:
                l = line.split(',')[0].strip()
                ret.append(l)
        return sets.Set(ret)

if __name__ == '__main__':
    components = read_component(sys.argv[1])
    for line in sys.stdin:
        component = line.strip() 
        if len(component) == 0 or component == 'none' or component in components:
            continue
        else:
            raise ValueError("Unable to find " + component + " in acceptable list of components: " + sys.argv[1])
