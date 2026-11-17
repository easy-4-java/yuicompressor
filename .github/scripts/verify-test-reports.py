"""Reject missing, empty, failed, skipped, duplicate or incomplete test reports."""
from pathlib import Path
import xml.etree.ElementTree as ET

reports = list(Path('target/surefire-reports').glob('TEST-*.xml'))
assert reports, 'No Surefire XML reports found'
expected = {
    'jargs.test.gnu.CmdLineParserTestCase': 11,
    'jargs.test.gnu.CustomOptionTestCase': 2,
    'com.yahoo.platform.yui.compressor.CompressionTest': 4,
}
seen = set()
counts = {}
for report in reports:
    suite = ET.parse(report).getroot()
    cases = suite.findall('testcase')
    assert cases, f'Empty test report: {report}'
    for case in cases:
        key = (case.attrib['classname'], case.attrib['name'])
        assert key not in seen, f'Duplicate test: {key}'
        assert not any(case.find(tag) is not None for tag in ('failure', 'error', 'skipped')), key
        seen.add(key)
        counts[key[0]] = counts.get(key[0], 0) + 1
for name, minimum in expected.items():
    assert counts.get(name, 0) >= minimum, f'Missing tests in {name}: {counts.get(name, 0)} < {minimum}'
print(f'Verified {len(seen)} unique passing tests across {len(reports)} reports')
